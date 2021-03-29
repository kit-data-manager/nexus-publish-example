# nexus-publish-example

This project is supposed to serve as a sample for the (semi-)automatic release and publishing 
workflow to Nexus. Exemplarily, the publication to Sonatype Nexus is shown, but it can be also 
used with any other Nexus instance. In order to automate manual interactions on the Nexus Web UI, 
e.g. closing and releasing Staging Repositories, the [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin)
is used.

## Project Structure

```
nexus-publish-example/
├── .gradle/
├── gradle/
│   ├── wrapper
│   ├── profile-release.gradle
├── src/
├── .gitignore
├── LICENSE
├── README.md
├── build.gradle 
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle
 ```
 
 The listing above shows the project structure. Relevant for the publishing process are the following files: 
 
 * profile-release.gradle - Contains publish/release specific configuration
 * build.gradle - Main build file including profile-release.gradle if release profile was selected
 * gradle.properties - Properties for Gradle containing the project version

## Prerequisites

In order to be able to publish to Sonatype Nexus in particular, a couple of prerequisites must be met. At first, you have to create 
a Sonatype OSSHR (OSS Repository Hosting) account. Afterwards, you have to create a project ticket for claiming the group id of your artifact.
For more details, please visit [the Sonatype OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html).

Furthermore, you will require a PGP key in order to be able to sign your release artifacts. For more information, please refer to the Sonatype article on 
[Working with PGP Signatures](https://central.sonatype.org/pages/working-with-pgp-signatures.html). Summarizing the article, with GPG installed, the 
following steps are required:

```
$ gpg --gen-key
gpg (GnuPG) 2.2.27; Copyright (C) 2021 g10 Code GmbH
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
[...]
pub   rsa3072 2021-03-29 [SC] [verfällt: 2023-03-29]
      D724FA7DAC5AA604464E2625A2957C69C4A94806
uid                      ###########################
sub   rsa3072 2021-03-29 [E] [verfällt: 2023-03-29]
$ gpg --keyserver hkp://pool.sks-keyservers.net --send-keys D724FA7DAC5AA604464E2625A2957C69C4A94806
gpg: sende Schlüssel A2957C69C4A94806 auf hkp://pool.sks-keyservers.net
$ gpg --list-keys --keyid-format SHORT
pub   rsa3072/C4A94806 2021-03-29 [SC] [verfällt: 2023-03-29]
      D724FA7DAC5AA604464E2625A2957C69C4A94806
uid      [ ultimativ ] ###########################
sub   rsa3072/D8FBDD6F 2021-03-29 [E] [verfällt: 2023-03-29]
$
```

Depending on your local GPG installation, you may have to replace `gpg` by `gpg2`.
In the last step of the listing, we also obtained the short form `C4A94806`  of the `secretKeyId` for better handling in the following steps.

Afterwards, your GPG key is published and can be used for signing and verifying artifact signatures. For using it for your deployment process, you have
now to export the key. This can be done by the following command:

```
$ mkdir ~/.gradle
$ gpg --export-secret-key C4A94806 > ~/.gradle/secring.gpg
$
```

During export, you have to provide the password you used while creating the key. We directly export the keyring to ~/.gradle as we will use it from there.
Finally, we have to create a file `~/.gradle/gradle.properties` with the following properties: 

```
signing.keyId=<Your secretKeyId, e.g. C4A94806>
signing.password=<Your GPG Key Password>
signing.secretKeyRingFile=~/.gradle/secring.gpg

ossrhUsername=<Your OSSRH Username>
ossrhPassword=<Your OSSRH Password>
```

:warning: **Store those properties only on your local machine and NEVER commit them to your Git repository. A way on how to transfer this process via GitHub is presented later.**

Now, that all prerequisites are met, we have to prepare for the first release.

## Release and Publish

Depending on what to release, this phase consists of one or two steps: 

1. Release the current snapshot
2. Perform the publication to Sonatype Nexus



.\gradlew.bat -Pdeploy release
