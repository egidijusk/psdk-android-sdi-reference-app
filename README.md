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

These are the PSDK-SDI apis which provides the usage for system related operations as mentioned below :

* sdiVersion
* abort current command
* serialNumber
* hardwareSerialNumber
* modelName
* pciRebootTime
* print html
* print bmp
* reboot
* hibernate
* shutdown

## EMV commands 
These commands/apis provide the EMV related operation for POS app.  

### EMV Contact Configuration
Can refer under sdi->config->CtConfig package

EMV contact configuration is necessary for EMV chip payment processing. Based on these config PSDK-SDI will process the payment.
In assets->config package we have emvct.json file, which contains the sample EMV config for contact.
This emvct.json file is mapped into EmvContactConfig model class and then can be loaded into terminal through CtConfig.

* Terminal Configuration 
* Application Configuration 
* CAPK Configuration

### EMV Contactless Configuration
Can refer under sdi->config->CtlsConfig package

EMV contactless configuration is necessary for EMV contactless payment processing. Based on these config PSDK-SDI will process the payment.
In assets->config package we have emvctls.json file, which contains the sample EMV config for contactless.
This emvctls.json file is mapped into EmvCtlsConfig model class and then can be loaded into terminal through CtlsConfig.

* Terminal Configuration
* Visa Application Configuration
* Mastercard Application Configuration
* Amex Application Configuration
* CAPK Configuration

### EMV Contact Transaction Flow
Can refer under sdi->card->SdiContact package

Here SdiContact process the api sequence flow for contact transaction based on Callback Mode and Re-entrance mode.
By default this reference app uses Re-entrance mode for payment processing which is handled from SdiContactAdvanced.
But Callback mode can be used as well as per the POS app requirement (This is processed from SdiContactBasic).

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
Can refer under sdi->card->SdiContactless package

Here SdiContactless process the api sequence flow for contactless transaction based on the contactless config loaded to terminal.

* Card Detect
* Soft LED Display
* re-tap scenario  
* First GEN AC
* TAG retrieval

### Magstripe Transaction Flow
Can refer under sdi->card->SdiSwipe package

Here SdiSwipe process the api sequence flow for magstripe transaction.

* Card Detect
* Validation checks
* TAG retrieval

## Manual Card Data entry
Can refer under sdi->card->SdiManual package

Here SdiManual process the api sequence flow for manual card entry transaction.

* PAN Entry
* Expiry Date
* CVV
* Exception handling

## Update Service
Can refer under ui->updateservice->UpdateServiceViewModel package

Here UpdateServiceViewModel uses the Update Service library apis for loading the packages ( zip, ota files ) to terminal.
These packages are stored under assets folder and will be loaded into terminal through Update Service apis.
We have shown the usage of few update service apis below for reference. For more details kindly check Update Service documentation.

* Install Apk
* Un-Install Apk
* Install Android Ota Package - Here we have a dummy file instead of the real OTA package to provide the reference for multiple terminals.
* Install Super Package(params, APK and Android OTA file)

## Data Interface (TBD) Highest priority
* Sample sccfg.json
* Sample VRK Payload
* https://confluence.verifone.com:8443/pages/viewpage.action?pageId=380320847
* https://bitbucket.verifone.com:8443/projects/IFADK/repos/dev-adk-sdi-client/browse/src/sdi-client.cpp

## Crypto Interface
Can refer under sdi->card->SdiSecureData package

Here SdiSecureData shows the sample usage for below mentioned crypto operations.

* Open Connection
* Crypto Component Versions
* Key Inventory Details
* Get Encrypted Pin
* Close Connection

## Pin Entry (TBD) Highest priority
* Pin entry with keypad (Need Android Device)
* Pin entry with Navigator

## SDI Plugin
* Showcase reading data from card which is read from plugin.a

## VCL  (Low priority) (TBD)

## NFC (TBD) // Not supported on Trinity

## VAS (TBD) // Not supported on Trinity


