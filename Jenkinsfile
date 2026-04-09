def VERSION_NAME
def VERSION_CODE

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

		stage('Clean') {
            steps {
                script {
                    sh './gradlew clean'
                }
            }
		}

		stage('Build AAR') {
            steps {
                script {
                    sh './gradlew :MorfinAuth:bundleReleaseAar'
                }
            }
		}

        stage('Build APK') {
            steps {
                script {
                     // Copy AAR File
                     def sourceFolder = "${WORKSPACE}/MorfinAuth/build/outputs/aar"
                     def destinationFolder = "${WORKSPACE}/app/libs"
                     bat "xcopy \"${sourceFolder}\\*.*\" \"${destinationFolder}\" /E /I /H /Y"

                    withCredentials([string(credentialsId: 'signingPassSecret', variable: 'SIGN_PASS'), string(credentialsId: 'signingAliasSecret', variable: 'ALIAS'), , string(credentialsId: 'signingFilePathSecret', variable: 'FILE_PATH')]) {
                        sh './gradlew -PkeyAlias=$ALIAS -PkeyPass=$SIGN_PASS -PstoreFilePath=$FILE_PATH -PstorePass=$SIGN_PASS :app:assembleRelease'
                    }

                    sh "./gradlew getVersionName"
                    VERSION_NAME = readFile('app/versionName.txt').trim()
                    sh "rm \"${WORKSPACE}/app/versionName.txt\""

                    sh "./gradlew getVersionCode"
                    VERSION_CODE = readFile('app/versionCode.txt').trim()
                    sh "rm \"${WORKSPACE}/app/versionCode.txt\""
               }
            }
        }

		stage('Deploy QC') {
            steps {
                script {
                    def buildVersion = env.BUILD_NUMBER
                    def currentJobName = env.JOB_NAME
					def ReleasePath = "\\\\192.168.10.133\\qa\\Morfin\\MorfinAuth\\${VERSION_NAME}"
                    
                    // Copy APK File For QC Team
                    def sourceFolder = "${WORKSPACE}/app/build/outputs/apk/release"
                    def destinationFolder = "${ReleasePath}/${buildVersion}"
					bat "xcopy \"${sourceFolder}\\*.*\" \"${destinationFolder}\" /E /I /H /Y"

					//Create Release Note For QC Team
                    def commitMessages = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
                    writeFile file: "${destinationFolder}/Release.txt", text: commitMessages
                }
            }
        }

		stage('Deploy Release') {
            steps {
                script {
                    def buildVersion = env.BUILD_NUMBER
                    def currentJobName = env.JOB_NAME
					def ReleasePath = "\\\\192.168.10.133\\MantraProductQAReleaseSource"
					def destinationFolder = "${ReleasePath}/MorfinAuth/${VERSION_NAME}/${buildVersion}"

                    // Copy APK
                    def sourceFolder = "${WORKSPACE}/app/build/outputs/apk/release"
                    def apkFolder = "${destinationFolder}/APK"
					bat "xcopy \"${sourceFolder}\\*.*\" \"${apkFolder}\" /E /I /H /Y"

					//Create Release Note
                    def commitMessages = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
                    writeFile file: "${destinationFolder}/Release.txt", text: commitMessages

                    //Clean Code
                    sh './gradlew clean'

					// Copy Final Source Code
                    def sourceFolder1 = "${WORKSPACE}"
                    def srcFolder = "${destinationFolder}/SourceCode/MorfinAuth_Sample"
                    bat "xcopy \"${sourceFolder1}\\*.*\" \"${srcFolder}\" /E /I /H /Y"

                    // Copy Final AAR Lib
                    def sourceFolder2 = "${WORKSPACE}/app/libs"
                    def libFolder = "${destinationFolder}/Libs"
                    bat "xcopy \"${sourceFolder2}\\*.*\" \"${libFolder}\" /E /I /H /Y"

                    //Delete Folder
                    def deleteModuleFolder = "${destinationFolder}/SourceCode/MorfinAuth_Sample/MorfinAuth"
                    if (fileExists("${deleteModuleFolder}")) {
                        bat "rmdir /s /q \"${deleteModuleFolder}\""
                    }
                    def deleteGitFolder = "${destinationFolder}/SourceCode/MorfinAuth_Sample/.git"
                    if (fileExists("${deleteGitFolder}")) {
                        bat "rmdir /s /q \"${deleteGitFolder}\""
                    }
                }
            }
        }

    }
}