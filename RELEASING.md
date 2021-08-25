# Steps to release

1. Update scala and sbt version
2. Merge the dev branch or do the release on dev branch(then ignore the next step) 
3. Create a branch for release from `master` branch
4. Update the esw `Version` in `Libs.scala` on release branch
5. Update version in `build.sbt` on release branch
6. Update `kotlin-plugin` version in `plugins.sbt` on release branch
7. Update version compatibility section in `README.md` (esw and csw version) on release branch
8. Merge release branch to `master` branch via PR. Use following commit message format for version upgrade:
    - `#major <commit message>`  : for upgrading major version.
    - `#minor <commit message>`  : for upgrading minor version. 
    - `#patch <commit message>`  : for upgrading patch version. 

**Note:** `PROD=true` environment variable needs to be set before running `release.sh`

**Note:** For *pre-release* tag using `releash.sh $VERSION$` as above plugin doesn't support our pre-release format.
    
