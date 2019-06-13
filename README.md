dokka  [![official JetBrains project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![TeamCity (build status)](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Kotlin_Dokka_DokkaAntMavenGradle)/statusIcon)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=Kotlin_Dokka_DokkaAntMavenGradle&branch_KotlinTools_Dokka=%3Cdefault%3E&tab=buildTypeStatusDiv) [ ![Download](https://api.bintray.com/packages/kotlin/dokka/dokka/images/download.svg) ](https://bintray.com/kotlin/dokka/dokka/_latestVersion)
=====

Dokka is a documentation engine for Kotlin, performing the same function as javadoc for Java.
Just like Kotlin itself, Dokka fully supports mixed-language Java/Kotlin projects. It understands
standard Javadoc comments in Java files and [KDoc comments](https://kotlinlang.org/docs/reference/kotlin-doc.html) in Kotlin files,
and can generate documentation in multiple formats including standard Javadoc, HTML and Markdown.

## Using Dokka

### Using the Gradle plugin

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.dokka:dokka-gradle-plugin:${dokka_version}"
    }
}
repositories {
    jcenter() // or maven { url 'https://dl.bintray.com/kotlin/dokka' }
}

apply plugin: 'org.jetbrains.dokka'
```

or using the plugins block:

```groovy
plugins {
    id 'org.jetbrains.dokka' version '0.9.18'
}
repositories {
    jcenter() // or maven { url 'https://dl.bintray.com/kotlin/dokka' }
}
```

The plugin adds a task named "dokka" to the project.

If you encounter any problems when migrating from older versions of Dokka, please see the [FAQ](https://github.com/Kotlin/dokka/wiki/faq).

Minimal dokka configuration:

Groovy
```groovy
dokka {
    outputFormat = 'html' 
    outputDirectory = "$buildDir/javadoc"
}
```

Kotlin
```kotlin
tasks {
    val dokka by getting(DokkaTask::class) {
        outputFormat = "html"
        outputDirectory = "$buildDir/dokka"
    }
}
```

[Output formats](#output_formats)

The available configuration options for single platform are shown below:

```groovy
dokka {
    moduleName = 'data'
    outputFormat = 'html'
    outputDirectory = "$buildDir/javadoc"
    
    // These tasks will be used to determine source directories and classpath 
    kotlinTasks {
        defaultKotlinTasks() + [':some:otherCompileKotlin', project("another").compileKotlin]
    }
    
    // List of files with module and package documentation
    // https://kotlinlang.org/docs/reference/kotlin-doc.html#module-and-package-documentation
    includes = ['packages.md', 'extra.md']
    
    // The list of files or directories containing sample code (referenced with @sample tags)
    samples = ['samples/basic.kt', 'samples/advanced.kt']
    
    jdkVersion = 6 // Used for linking to JDK
    
    impliedPlatforms = ["JVM"] // See platforms section of documentation 

    // Use default or set to custom path to cache directory
    // to enable package-list caching
    // When set to default, caches stored in $USER_HOME/.cache/dokka
    cacheRoot = 'default' 
    
    configuration {
        // Use to include or exclude non public members.
        includeNonPublic = false
        
        // Do not output deprecated members. Applies globally, can be overridden by packageOptions
        skipDeprecated = false 
       
        // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
        reportUndocumented = true 
        
        skipEmptyPackages = true // Do not create index pages for empty packages
     
        targets = ["JVM"] // See platforms section of documentation 

        platform = "JVM" // Platform used for code analysis 
        
        // Manual adding files to classpath
        // This property not overrides classpath collected from kotlinTasks but appends to it
        classpath = [new File("$buildDir/other.jar")]
    
        // By default, sourceRoots is taken from kotlinTasks, following roots will be appended to it
        // Short form sourceRoots
        sourceDirs = files('src/main/kotlin')
        
        // By default, sourceRoots is taken from kotlinTasks, following roots will be appended to it
        // Full form sourceRoot declaration
        // Repeat for multiple sourceRoots
        sourceRoot {
            // Path to source root
            path = "src" 
        }
        
        // Specifies the location of the project source code on the Web.
        // If provided, Dokka generates "source" links for each declaration.
        // Repeat for multiple mappings
        sourceLink {
            // Unix based directory relative path to the root of the project (where you execute gradle respectively). 
            path = "src/main/kotlin" // or simply "./"
             
            // URL showing where the source code can be accessed through the web browser
            url = "https://github.com/cy6erGn0m/vertx3-lang-kotlin/blob/master/src/main/kotlin" //remove src/main/kotlin if you use "./" above
            
            // Suffix which is used to append the line number to the URL. Use #L for GitHub
            lineSuffix = "#L"
        }
        
        // Disable linking to online kotlin-stdlib documentation
        noStdlibLink = false
        
        // Disable linking to online JDK documentation
        noJdkLink = false 
        
        // Allows linking to documentation of the project's dependencies (generated with Javadoc or Dokka)
        // Repeat for multiple links
        externalDocumentationLink {
            // Root URL of the generated documentation to link with. The trailing slash is required!
            url = new URL("https://example.com/docs/")
            
            // If package-list file located in non-standard location
            // packageListUrl = new URL("file:///home/user/localdocs/package-list") 
        }
        
        // Allows to customize documentation generation options on a per-package basis
        // Repeat for multiple packageOptions
        perPackageOption {
            prefix = "kotlin" // will match kotlin and all sub-packages of it
            // All options are optional, default values are below:
            skipDeprecated = false
            reportUndocumented = true // Emit warnings about not documented members 
            includeNonPublic = false
        }
        // Suppress a package
        perPackageOption {
            prefix = "kotlin.internal" // will match kotlin.internal and all sub-packages of it
            suppress = true
        }
    }
}
```

#### Multiplatform
Since 0.9.19 dokka supports multiplatform projects. 
The available configuration options for multiplatform are like those for single platform, but the `configuration` block 
is replaced by `multiplatform` which has inner blocks for each platform. The inner blocks can be named arbitrary, 
however if you want to use source roots and classpath provided by Kotlin Multiplatform plugin, they must have the same names.

Groovy
```groovy
kotlin { // Kotlin Multiplatform plugin configuration
    jvm() 
    js("customName")
}

dokka {
    outputDirectory = "$buildDir/dokka"
    outputFormat = "html"

    multiplatform {
        customName { // The same name as in Kotlin Multiplatform plugin, so the sources are fetched automatically
            targets = ["JS"]
            platform = "js"
        }

        differentName { // Different name, so source roots must be passed explicitly
            targets = ["JVM"]
            platform = "jvm"
            sourceRoot {
                path = kotlin.sourceSets.jvmMain.kotlin.srcDirs[0]
            }
            sourceRoot {
                path = kotlin.sourceSets.commonMain.kotlin.srcDirs[0]
            }
        }
    }
}
```

Kotlin
```kotlin
kotlin {  // Kotlin Multiplatform plugin configuration
    jvm()
    js("customName")
}

val dokka by getting(DokkaTask::class) {
        outputDirectory = "$buildDir/dokka"
        outputFormat = "html"

        multiplatform { 
            val customName by creating { // The same name as in Kotlin Multiplatform plugin, so the sources are fetched automatically
                targets = listOf("JS")
                platform = "js"
            }

            register("differentName") { // Different name, so source roots must be passed explicitly
                targets = listOf("JVM")
                platform = "jvm"
                sourceRoot {
                    path = kotlin.sourceSets.getByName("jvmMain").kotlin.srcDirs.first().toString()
                }
                sourceRoot {
                    path = kotlin.sourceSets.getByName("commonMain").kotlin.srcDirs.first().toString()
                }
            }
        }
    }
```

Note that `javadoc` output format cannot be used with multiplatform. 

To generate the documentation, use the `dokka` Gradle task:

```bash
./gradlew dokka
```

More dokka tasks can be added to a project like this:

```groovy
task dokkaJavadoc(type: org.jetbrains.dokka.gradle.DokkaTask) {
    outputFormat = 'javadoc'
    outputDirectory = "$buildDir/javadoc"
}
```

Please see the [Dokka Gradle example project](https://github.com/JetBrains/kotlin-examples/tree/master/gradle/dokka-gradle-example) for an example.

#### Dokka Runtime
If you are using Gradle plugin and you want to use a custom version of Dokka, you can do it by setting `dokkaRuntime` and `dokkaFatJar`:

```groovy
buildscript {
    ...
}

apply plugin: 'org.jetbrains.dokka'

repositories {
    jcenter()
}

dependencies {
    dokkaRuntime "org.jetbrains.dokka:dokka-fatjar:0.9.19"
}

dokka {
    dokkaFatJar = "dokka-fatjar-0.9.19"
    outputFormat = 'html'
    outputDirectory = "$buildDir/dokkaHtml"
}

```
If you don't provide `dokkaFatJar` in `dokka` task then version from `dokka-gradle-plugin` will be used.  

To use your Fat Jar, just set path to it:

 ```groovy
 buildscript {
     ...
 }
 
 apply plugin: 'org.jetbrains.dokka'
 
 repositories {
     jcenter()
 }
 
 dependencies {
     dokkaRuntime files("/path/to/fatjar/dokka-fatjar-0.9.19.jar")
 }
 
 dokka {
     dokkaFatJar = "dokka-fatjar-0.9.19"
     outputFormat = 'html'
     outputDirectory = "$buildDir/dokkaHtml"
 }
 
 ```

#### FAQ
If you encounter any problems, please see the [FAQ](https://github.com/Kotlin/dokka/wiki/faq).

#### Android

If you are using Android there is a separate Gradle plugin. Just make sure you apply the plugin after
`com.android.library` and `kotlin-android`.

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.dokka:dokka-android-gradle-plugin:${dokka_version}"
    }
}
repositories {
    jcenter()
}
apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'
apply plugin: 'org.jetbrains.dokka-android'
```

### Using the Maven plugin

The Maven plugin is available in JCenter. You need to add the JCenter repository to the list of plugin repositories if it's not there:

```xml
<pluginRepositories>
    <pluginRepository>
        <id>jcenter</id>
        <name>JCenter</name>
        <url>https://jcenter.bintray.com/</url>
    </pluginRepository>
</pluginRepositories>
```

Minimal Maven configuration is

```xml
<plugin>
    <groupId>org.jetbrains.dokka</groupId>
    <artifactId>dokka-maven-plugin</artifactId>
    <version>${dokka.version}</version>
    <executions>
        <execution>
            <phase>pre-site</phase>
            <goals>
                <goal>dokka</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

By default files will be generated in `target/dokka`.

The following goals are provided by the plugin:

  * `dokka:dokka` - generate HTML documentation in Dokka format (showing declarations in Kotlin syntax);
  * `dokka:javadoc` - generate HTML documentation in JavaDoc format (showing declarations in Java syntax);
  * `dokka:javadocJar` - generate a .jar file with JavaDoc format documentation.

The available configuration options are shown below:

```xml
<plugin>
    <groupId>org.jetbrains.dokka</groupId>
    <artifactId>dokka-maven-plugin</artifactId>
    <version>${dokka.version}</version>
    <executions>
        <execution>
            <phase>pre-site</phase>
            <goals>
                <goal>dokka</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
    
        <!-- Set to true to skip dokka task, default: false -->
        <skip>false</skip>
    
        <!-- Default: ${project.artifactId} -->
        <moduleName>data</moduleName>
        <!-- See list of possible formats below -->
        <outputFormat>html</outputFormat>
        <!-- Default: ${project.basedir}/target/dokka -->
        <outputDir>some/out/dir</outputDir>
        
        <!-- Use default or set to custom path to cache directory to enable package-list caching. -->
        <!-- When set to default, caches stored in $USER_HOME/.cache/dokka -->
        <cacheRoot>default</cacheRoot>

        <!-- List of '.md' files with package and module docs -->
        <!-- https://kotlinlang.org/docs/reference/kotlin-doc.html#module-and-package-documentation -->
        <includes>
            <file>packages.md</file>
            <file>extra.md</file>
        </includes>
        
        <!-- List of sample roots -->
        <samples>
            <dir>src/test/samples</dir>
        </samples>
        
        <!-- Used for linking to JDK, default: 6 -->
        <jdkVersion>6</jdkVersion>

        <!-- Do not output deprecated members, applies globally, can be overridden by packageOptions -->
        <skipDeprecated>false</skipDeprecated> 
        <!-- Emit warnings about not documented members, applies globally, also can be overridden by packageOptions -->
        <reportUndocumented>true</reportUndocumented>             
        <!-- Do not create index pages for empty packages -->
        <skipEmptyPackages>true</skipEmptyPackages> 
        
        <!-- See platforms section of documentation -->
        <impliedPlatforms>
            <platform>JVM</platform>
        </impliedPlatforms>
        
        <!-- Short form list of sourceRoots, by default, set to ${project.compileSourceRoots} -->
        <sourceDirectories>
            <dir>src/main/kotlin</dir>
        </sourceDirectories>
        
        <!-- Full form list of sourceRoots -->
        <sourceRoots>
            <root>
                <path>src/main/kotlin</path>
                <!-- See platforms section of documentation -->
                <platforms>JVM</platforms>
            </root>
        </sourceRoots>
        
        <!-- Specifies the location of the project source code on the Web. If provided, Dokka generates "source" links
             for each declaration. -->
        <sourceLinks>
            <link>
                <!-- Source directory -->
                <path>${project.basedir}/src/main/kotlin</path>
                <!-- URL showing where the source code can be accessed through the web browser -->
                <url>https://github.com/cy6erGn0m/vertx3-lang-kotlin/blob/master/src/main/kotlin</url> <!-- //remove src/main/kotlin if you use "./" above -->
                <!--Suffix which is used to append the line number to the URL. Use #L for GitHub -->
                <lineSuffix>#L</lineSuffix>
            </link>
        </sourceLinks>
        
        <!-- Disable linking to online kotlin-stdlib documentation  -->
        <noStdlibLink>false</noStdlibLink>
        
        <!-- Disable linking to online JDK documentation -->
        <noJdkLink>false</noJdkLink>
        
        <!-- Allows linking to documentation of the project's dependencies (generated with Javadoc or Dokka) -->
        <externalDocumentationLinks>
            <link>
                <!-- Root URL of the generated documentation to link with. The trailing slash is required! -->
                <url>https://example.com/docs/</url>
                <!-- If package-list file located in non-standard location -->
                <!-- <packageListUrl>file:///home/user/localdocs/package-list</packageListUrl> -->
            </link>
        </externalDocumentationLinks>

        <!-- Allows to customize documentation generation options on a per-package basis -->
        <perPackageOptions>
            <packageOptions>
                <!-- Will match kotlin and all sub-packages of it -->
                <prefix>kotlin</prefix>
                
                <!-- All options are optional, default values are below: -->
                <skipDeprecated>false</skipDeprecated>
                <!-- Emit warnings about not documented members  -->
                <reportUndocumented>true</reportUndocumented>
                <includeNonPublic>false</includeNonPublic>
            </packageOptions>
        </perPackageOptions>
    </configuration>
</plugin>
```

Please see the [Dokka Maven example project](https://github.com/JetBrains/kotlin-examples/tree/master/maven/dokka-maven-example) for an example.

[Output formats](#output_formats)

### Using the Ant task

The Ant task definition is also contained in the dokka-fatjar.jar referenced above. Here's an example of using it:

```xml
<project name="Dokka" default="document">
    <typedef resource="dokka-antlib.xml" classpath="dokka-fatjar.jar"/>

    <target name="document">
        <dokka src="src" outputdir="doc" modulename="myproject"/>
    </target>
</project>
```

The Ant task supports the following attributes:

  * `outputDir` - the output directory where the documentation is generated
  * `format` - the output format (see the list of supported formats above)
  * `classpath` - list of directories or .jar files to include in the classpath (used for resolving references)
  * `samples` - list of directories containing sample code (documentation for those directories is not generated but declarations from them can be referenced using the `@sample` tag)
  * `moduleName` - the name of the module being documented (used as the root directory of the generated documentation)
  * `include` - names of files containing the documentation for the module and individual packages
  * `skipDeprecated` - if set, deprecated elements are not included in the generated documentation
  * `jdkVersion` - version for linking to JDK
  * `impliedPlatforms` - See [platforms](#platforms) section
  * `<sourceRoot path="src" platforms="JVM" />` - analogue of src, but allows to specify [platforms](#platforms) 
  * `<packageOptions prefix="kotlin" includeNonPublic="false" reportUndocumented="true" skipDeprecated="false"/>` - 
    Per package options for package `kotlin` and sub-packages of it
  * `noStdlibLink` - disable linking to online kotlin-stdlib documentation
  * `noJdkLink` - disable linking to online JDK documentation
  * `<externalDocumentationLink url="https://example.com/docs/" packageListUrl="file:///home/user/localdocs/package-list"/>` -
    linking to external documentation, packageListUrl should be used if package-list located not in standard location
  * `cacheRoot` - Use `default` or set to custom path to cache directory to enable package-list caching. When set to `default`, caches stored in $USER_HOME/.cache/dokka


### Using the Command Line

To run Dokka from the command line, download the [Dokka jar](https://github.com/Kotlin/dokka/releases/download/0.9.10/dokka-fatjar.jar).
To generate documentation, run the following command:

    java -jar dokka-fatjar.jar <source directories> <arguments>

Dokka supports the following command line arguments:

  * `-output` - the output directory where the documentation is generated
  * `-format` - the [output format](#output-formats):
  * `-classpath` - list of directories or .jar files to include in the classpath (used for resolving references)
  * `-samples` - list of directories containing sample code (documentation for those directories is not generated but declarations from them can be referenced using the `@sample` tag)
  * `-module` - the name of the module being documented (used as the root directory of the generated documentation)
  * `-include` - names of files containing the documentation for the module and individual packages
  * `-nodeprecated` - if set, deprecated elements are not included in the generated documentation
  * `-impliedPlatforms` - list of implied platforms (comma-separated)
  * `-packageOptions` - list of package options in format `prefix,-deprecated,-privateApi,+warnUndocumented;...` 
  * `-links` - external documentation links in format `url^packageListUrl^^url2...`
  * `-noStdlibLink` - disable linking to online kotlin-stdlib documentation
  * `-noJdkLink` - disable linking to online JDK documentation
  * `-cacheRoot` - use `default` or set to custom path to cache directory to enable package-list caching. When set to `default`, caches stored in $USER_HOME/.cache/dokka


### Output formats<a name="output_formats"></a>

  * `html` - minimalistic html format used by default, Java classes are translated to Kotlin
  * `javadoc` - looks like normal Javadoc, Kotlin classes are translated to Java
  * `html-as-java` - looks like `html`, but Kotlin classes are translated to Java
  * `markdown` - markdown structured as `html`, Java classes are translated to Kotlin
    * `gfm` - GitHub flavored markdown
    * `jekyll` - Jekyll compatible markdown 
  * `kotlin-website*` - internal format used for documentation on [kotlinlang.org](https://kotlinlang.org)

### Platforms<a name="platforms"></a>

Dokka can annotate elements with special `platform` block with platform requirements 

Example of usage can be found on [kotlinlang.org](https://kotlinlang.org/api/latest/jvm/stdlib/)

Each source root has a list of platforms for which members are suitable. 
Also, the list of 'implied' platforms is passed to Dokka.
If a member is not available for all platforms in the implied platforms set, its documentation will show
the list of platforms for which it's available.

## Dokka Internals

### Documentation Model

Dokka uses Kotlin-as-a-service technology to build `code model`, then processes it into `documentation model`.
`Documentation model` is graph of items describing code elements such as classes, packages, functions, etc.

Each node has semantic attached, e.g. Value:name -> Type:String means that some value `name` is of type `String`.

Each reference between nodes also has semantic attached, and there are three of them:

1. Member - reference means that target is member of the source, form tree.
2. Detail - reference means that target describes source in more details, form tree.
3. Link - any link to any other node, free form.

Member & Detail has reverse Owner reference, while Link's back reference is also Link.

Nodes that are Details of other nodes cannot have Members.

### Rendering Docs

When we have documentation model, we can render docs in various formats, languages and layouts. We have some core services:

* FormatService -- represents output format
* LocationService -- represents folder and file layout
* SignatureGenerator -- represents target language by generating class/function/package signatures from model

Basically, given the `documentation` as a model, we do this:

```kotlin
    val signatureGenerator = KotlinSignatureGenerator()
    val locationService = FoldersLocationService(arguments.outputDir)
    val markdown = JekyllFormatService(locationService, signatureGenerator)
    val generator = FileGenerator(signatureGenerator, locationService, markdown)
    generator.generate(documentation)
```

## Building Dokka

Dokka is built with Gradle. To build it, use `./gradlew build`.
Alternatively, open the project directory in IntelliJ IDEA and use the IDE to build and run Dokka.
