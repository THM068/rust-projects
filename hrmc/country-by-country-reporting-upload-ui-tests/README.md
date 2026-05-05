# country-by-country-reporting-upload-ui-tests
UI test suite for the `country-by-country-reporting-frontend` using WebDriver and `cucumber`.  

## Pre-requisites

## Services

Start Mongo Docker container as follows:

```bash
docker run --rm -d -p 27017:27017 --name mongo percona/percona-server-mongodb:5.0
```

Run the following command to start CBC services locally:

```bash
    sm2 --start CBCR_NEW_ALL
```
Starting a large group of services in a profile can overload the cpu of a machine and lead to services failing to start.
If this happens use one, or a combination of the following arguments: `--delay-seconds 5` to include a delay of 5
seconds between sm2 starting each service and `--workers 1` to force sm2 to only start one service at a time.
```bash
   sm2 --start CBCR_NEW_ALL --delay-seconds 5 --workers 1
```

### Selenium Manager

Confirm that [ui-test-runner](https://github.com/hmrc/ui-test-runner) is up-to-date and follow the provided [instructions](https://github.com/hmrc/ui-test-runner/blob/main/README.md).

### Test inspection and debugging

The system property `browser.option.headless` is defaulted to `true` in ui-test-runner. To view tests running locally in
the browser UI for debugging, set the system property `-Dbrowser.option.headless=false` in the sbt command.

## Tests

Run tests as follows:
* Argument `<browser>` must be `chrome`, `edge`, or `firefox`.
* Argument `<environment>` must be `local`.

```bash
./run_tests.sh <browser> <environment> 
```
The `run_tests.sh` script defaults to using `chrome` in the `local` environment.  For a complete list of supported param values, see:
- `src/test/resources/application.conf` for **environment**. Existing configuration supports execution only on local environment.

## Additional information to run acceptance tests

* To run any test individually, add "@solo" tag to the respective file and use ./run_solo.sh script. This script
defaults to using `chrome` in the `local` environment.

```bash
./run_solo.sh <browser> <environment>
```

### Running ZAP tests

ZAP tests can be automated using the HMRC Dynamic Application Security Testing approach. Running
automated ZAP tests should not be considered a substitute for manual exploratory testing using OWASP ZAP.

#### Executing a local ZAP test

First [run the DAST tool locally](https://github.com/hmrc/dast-config-manager/blob/main/README.md#running-zap-locally)

The shell script `run_zap_local.sh` is available to execute ZAP tests. The script proxies the journeys tagged with 'ZapTests' via ZAP.

For example, to execute ZAP tests locally using Chrome browser:

```bash
./run_zap_local.sh chrome local
```

### Scalafmt
This repository uses [Scalafmt](https://scalameta.org/scalafmt/), a code formatter for Scala. The formatting rules configured for this repository are defined within [.scalafmt.conf](.scalafmt.conf).

To apply formatting to the project files in this repository using the configured rules in [.scalafmt.conf](.scalafmt.conf) execute:

```bash
sbt scalafmtAll
```

To format the `*.sbt` and `project/*.scala` files, use:

```bash
sbt scalafmtSbt
```

To check files have been formatted as expected execute:

```bash
 sbt scalafmtCheckAll scalafmtSbtCheck
```
[Visit the official Scalafmt documentation to view a complete list of tasks which can be run.](https://scalameta.org/scalafmt/docs/installation.html#task-keys)
