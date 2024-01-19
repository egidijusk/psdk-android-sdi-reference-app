# psdk_sdi

PSDK SDI  Android Kotlin

Disclaimer: this test app is not to show case application architecture, it is to showcase the various SDI commands which are widely used  as part of payment application development.


# Logging
 
When you start a transaction and  filter for <b>"Command"</b> and show only selected application, 
the commands being executed will be shown in the logs along with the result after being processed,
this will give you a high level overview of the Sdi Command sequence

When raising JSD tickets, please enable specific component logs and attach them as part of ticket 

Below instructions are only for onDevice Trinity (T650) terminal integrations for other integration
models please contact Verifone support team for assistance 

## General for all issues
Enable SDI logs (SDI_log.conf is included in root directory of this project)

adb push SDI_log.conf /sdcard/config/adk-log

adb reboot

## EMV Contact related issues 
Enable EMVCT logs ( EMVCT_log.conf is included in root directory of this project)

adb push EMVCT_log.conf /sdcard/config/adk-log

adb reboot 

## EMV Contactless related issues
Enable EMVCTLS logs ( EMVCTLS_log.conf is included in root directory of this project)

adb push EMVCTLS_log.conf /sdcard/config/adk-log

adb reboot

## Security Api related issues

Enable SEC logs ( SEC_log.conf.conf is included in root directory of this project)

adb push SEC_log.conf /sdcard/config/adk-log

adb reboot

perform your transaction sequence , capture logs and share in ticket

Note: Enabling logs will reduce performance, please disable them after the test.

You can change the enable flag to false in the respective conf file and push the file to terminal.

# Source code

Please refer the <b>sdi</b>  package for the usage of PSDK SDI apis, all other packages are very 
specific to the test app and will not be much help for your integration.

# Feature list

## System Commands 
Can refer under sdi->system->SdiSystem package

* sdiVersion
* abort current command
* serialNumber
* hardwareSerialNumber
* modelName
* pciRebootTime
* print html
* print bmp (TBD need proper image)
* reboot
* hibernate
* shutdown

## EMV commands 

### EMV Contact Configuration

* Terminal Configuration 
* Application Configuration 
* CAPK Configuration

### EMV Contactless Configuration

* Terminal Configuration
* Visa Application Configuration
* Mastercard Application Configuration
* Amex Application Configuration
* CAPK Configuration

### EMV Contact Transaction Flow

* Card Detect ( This covers MSR as well )
* Activate
* Callback Mode and Re-entrance mode  
* Application Selection
* READ record callback
* PIN CVM (offline and online)
* TAG retrieval  
* First GEN AC
* Second GEN AC
* Wait for Card Removal (TBD)

### EMV Contactless Transaction Flow

* Card Detect
* Soft LED Display
* re-tap scenario  
* First GEN AC
* TAG retrieval

### Magstripe Transaction Flow

* Card Detect
* Validation checks
* TAG retrieval

## Manual Card Data entry

* PAN Entry
* Expiry Date
* CVV
* Exception handling

## Update Service

* Install Apk
* Un-Install Apk
* Install Android Ota Package - Here we have a dummy file instead of the real OTA package to provide the reference for multiple terminals.
* Install Super Package(params, APK and Android OTA file)

## Data Interface (TBD) Highest priority
* Sample sccfg.json
* Sample VRK Payload
* https://confluence.verifone.com:8443/pages/viewpage.action?pageId=380320847
* https://bitbucket.verifone.com:8443/projects/IFADK/repos/dev-adk-sdi-client/browse/src/sdi-client.cpp

## Crypto Interface (TBD) Highest priority

## Pin Entry (TBD) Highest priority
* Pin entry with keypad (Need Android Device)
* Pin entry with Navigator

## SDI Plugin
* Showcase reading data from card which is read from plugin.a

## VCL  (Low priority) (TBD)

## NFC (TBD) // Not supported on Trinity

## VAS (TBD) // Not supported on Trinity


