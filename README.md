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

:warning: **Store those properties only on your local machine and NEVER commit them to your Git repository. A way on how to transfer this process via GitHub is presented later.**:warning:

Now, that all prerequisites are met, we have to prepare for the first release.

## Release and Publish

Depending on what to release, this phase consists of one or two steps: 

1. Release the current snapshot
2. Perform the publication to Sonatype Nexus

### 1. Release

In order so release your current snapshot, e.g. assigning and tagging a version, we are first using the [gradle-release plugin](https://github.com/researchgate/gradle-release). Everything you need for releasing is configured inside `gradle/profile-release.properties` under `release{}`. There, the tag template, e.g. `'v${version}'` for tags in the format `v1.0.0`, as well as source file and properties for the version number, are configured. You can trigger a release by calling: 

```
$ ./gradlew -Prelease release
[...]
$
```
During this process you'll be asked for the version number of the release as well as for the next snaphot version number. However, you may let the plugin assign 
version automatically by providing the `-Prelease.useAutomaticVersion=true` command line switch.
After release, you are ready for publishing the released version to Sonatype Nexus.

### 2. Publish

For publishing you should checkout/switch to the tag created in the previous step unless you want to publish the current snapshot.

```
$git checkout tags/v1.0.0
HEAD is now at XXXXXXX [Gradle Release Plugin] - pre tag commit:  'v1.0.0'.
$
```

In order to trigger the publication process, execute the following Gradle command:

```
 $./gradlew -Prelease publishToSonatype closeAndReleaseSonatypeStagingRepository
```

Assuming that all steps before were carried out properly, this should start the publication process including:

1. Creating a Staging Repository at Sonatype Nexus
2. Building and signing all artifacts of the release, e.g. library, sources and javadoc
3. Creating and uploading pom.xml based on the inputs in `gradle/profile-release.properties` under `publishing {}`
4. Close the created Staging Repository triggering Sonatype Validation
5. Release the contents of the Staging Repository

If everything worked properly, e.g. all artifacts were present, signature validation and pom.xml validation succeeded, you should be able to find your published library at 
[Maven Central](https://search.maven.org/) after some time.

## Automated publishing via GitHub Actions

If you want to automate your publishing process, GitHub Actions are a good option to do so. In this example we assume, that only releases are published as soon as they are created on GitHub. Those instructions are contained in file `.github/workflow/publishRelease.yml`. Let's have a short look at the most important elements of this file:

```
on:
  release:
    types: [created]
```

This section states, that the action is triggered as soon as a GitHub Release was created. This means, you first have to perform the `Release` step above before you have to create a release for the created tag manually via GitHub, which allows you to add a proper description, e.g. the changelog, and a release name. As soon as you published the release the GitHub Action is triggered for setting up its environment and finally calling the next relevant line:

```
run: gradle -Prelease publishToSonatype closeAndReleaseSonatypeStagingRepository
```

This line contains actual command which is executed by the action. As we want to perform the publication to Sonatype Nexus we just use the same command as for the publication from our local machine. But how is the execution parameterized? This is done within the next section, which is probably the most important one:

```
env:
           ORG_GRADLE_PROJECT_sonatypeUsername : ${{ secrets.OSSRH_USERNAME }}
           ORG_GRADLE_PROJECT_sonatypePassword : ${{ secrets.OSSRH_PASSWORD }}
           ORG_GRADLE_PROJECT_signingKey : ${{ secrets.SIGNING_KEY }}
           ORG_GRADLE_PROJECT_signingPassword : ${{ secrets.SIGNING_SECRET }}
```

Here you see the main difference between publishing from your local machine and via GitHub Action, which is the way on how to provide secrets, e.g. `ossrhUsername`, `ossrhPassword`, as well as all information required for signing your artifacts. If something goes wrong during the build process on GitHub, these elements might be good candidates for a closer look.

In order so provide secret information for the build process, GitHub offers a way to define `Secrets`, which are only visible once at creation time and to the build process. To create them, navigate to your project settings and select `Secrets` on the left hand side. In the upper right corner you'll find a button `New repository secret` used to add a new secret consisting of `Name` and `Value`. The secret names can then be used in your GitHub Action definition by assigning them to environment variables available within the build context.

:information_source: GitHub Secrets are presented to the build process as variables in the form ${{secrets.<SECRET_NAME>}}. In order to use them easily within your Gradle build process, you'll have to map them to appropriate environment variables, e.g. `ORG_GRADLE_PROJECT_signingKey`. :information_source:

For this example project, the following secrets are required:

| Name  | Desription  | Environment Variable  |
|---|---|---|
| OSSRH_USERNAME  | Your Sonatype Nexus username  | ORG_GRADLE_PROJECT_sonatypeUsername  |
| OSSRH_PASSWORD  | Your Sonatype Nexus password |  ORG_GRADLE_PROJECT_sonatypePassword |
| SIGNING_KEY |  Your GPG ascii-armored private key  | ORG_GRADLE_PROJECT_signingKey  |
| SIGNING_PASSWORD |  Your GPG private key password  | ORG_GRADLE_PROJECT_signingPassword  |

If you have multiple projects to publish within one organization you may also define GitHub Secrets on organization level in order to make them available to all projects. You may also change the names of those secrets at creation time, but if you do so, you also have to change `.github/workflows/publishRelease.yml` in order to assign the proper values to the required environment variables.

For now, the only remaining question is, how to obtain an ascii-armored version of your private GPG key. This can be done using the following command:

```
$ gpg --armor --export-secret-keys C4A94806 > ascii.key
$
```

Again, depending on your GPG installation you may have to replace `gpg` by `gpg2`.

Before the export is done, you'll be asked for the password to encrypt your private key, which is the same as you used before in `~/.gradle/gradle.properties` as property `signing.password` and which you'll have to provide as GitHub Secret `SIGNING_PASSWORD`. As a result, the file `ascii.key` contains your ascii-armored private key which you can now provide as GitHub Secret as `SIGNING_KEY`

Well, that's it. Now you should be able to perform the local release (see above), create a GitHub Release based on the new tag and thus trigger the Publish process to Sonatype Nexus.

## Troubleshooting

:question: While publishing I receive 'Failed to publish publication 'maven' to repository 'sonatype' [...] Received status code 403 from server: Forbidden'

:grey_exclamation: Check the `nexusPublishing{}` section in `gradle/profile-release.properties`. Using the default Sonatype settings, e.g. sonatype(), only works, if you registered at Sonatype before 24 Feb 2021. Otherwise or if your are using another Nexus instance, you have to overwrite `nexusUrl` and `snapshotRepositoryUrl` as stated in the file. If everything is correct, please also check `~/.gradle/gradle.properties` for containing both properties `ossrhUsername` and `ossrhPassword` matching the adressed Sonatype Nexus instance.
##

:question: I'm getting some error stating that there are signatory issues, e.g. [...]it has no configured signatory or 'WARNING: No property 'signingKey' found. Artifact signing will be skipped.'.

:grey_exclamation: Check the prerequisites described above. In the most simple case, you'll need a GPG key, which must be published, and the according properties inside `~/.gradle/gradle.properties`.
##
