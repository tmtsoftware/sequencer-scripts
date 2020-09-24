# Steps to release

1. Update the esw `Version` in `Libs.scala`
2. Update `kotlin-plugin` version in `plugins.sbt`
2. Update version compatibility section in README.md (esw and csw version)
3. Commit for *FINAL* release with version you want to upgrade
    example: 
    - `#major <commit message>`  : for upgrading major version.
    - `#minor <commit message>`  : for upgrading minor version. 
    - `#patch <commit message>`  : for upgrading patch version. 

Note: For *pre-release* tag using `releash.sh $VERSION$` as above plugin doesn't support our pre-release format.
    
