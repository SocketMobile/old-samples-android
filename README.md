# Socket Mobile Android Samples

### Usage

To build the sample applications, download the Android ScanAPI SDK from the [Socket Mobile developer portal](https://www.socketmobile.com/developers) and copy `scanapi.jar` and `scanapi-api.jar` into the `libs` folder of each sample project (e.g. `singleEntry/libs`).

## Single Entry

Single Entry is a minimal example of a ScanAPI enabled Android app. It uses a
custom `Application` class to share a single instance of ScanAPI among its
activities. Opening ScanAPI and connecting a device are expensive operations,
so keeping ScanAPI open across configuration changes and sharing it between
activities offers the best user experience.

Before ScanAPI can interact with your scanner, you must make a few configuration
changes to your scanner:

1. **Put your scanner in SPP Mode** - Scanners ship in HID mode, by default. Use
the command barcode found in your scanner's quick start guide to switch to SPP
mode
2. **Pair your scanner** - Using your device's Bluetooth connection manager, 
establish a pairing between your device and the scanner 
3. **Configure scanner to initiate connection** - The first interaction between
ScanAPI and your scanner. Since the scanner doesn't have a screen to list
discoverable devices nearby, the scanner ships in acceptor mode and is
discoverable for a few minutes after being powered on. However, once the scanner
has been paired it needs to be put in initiator mode so that it can connect and
reconnect to your device as quickly and efficiently as possible

> **Note**: In most cases, it is easiest to use a dynamically generated barcode
> to make the scanner perform the last two steps automatically. See your
> scanner's *Command Barcode Sheet* for instructions on generating a barcode.

More information about configuring your scanner can be found in your scanner's
*Quick Start Guide*

* 7 Series 1D Imager (7Ci/7Di)
    * [Quick Start Guide](http://www.socketmobile.com/docs/default-source/series-7/chs-1d-imager-quick-start-guide.pdf?sfvrsn=6)
    * [Command Barcode Sheet](http://www.socketmobile.com/docs/default-source/series-7/chs-1d-imager-command-barcodes-sheet.pdf?sfvrsn=10)
* 7 Series 1D Laser (7Mi/7Pi)
    * [Quick Start Guide](http://www.socketmobile.com/docs/default-source/series-7/chs-1d-laser-quick-start-guide.pdf?sfvrsn=2)
    * [Command Barcode Sheet](http://www.socketmobile.com/docs/default-source/default-document-library/\(1d\)-laser-command-barcode-sheet.pdf?sfvrsn=0)
* 7 Series 2D Imager (7Qi/7Xi)
    * [Quick Start Guide](http://www.socketmobile.com/docs/default-source/series-7/chs-2d-quick-start-guide.pdf?sfvrsn=6)
    * [Command Barcode Sheet](http://www.socketmobile.com/docs/default-source/series-7/command-barcode-sheet-2d.pdf?sfvrsn=4)
* 8 Series (8Ci/8Qi)
    * [Quick Start Guide](http://www.socketmobile.com/docs/default-source/series-8-brochures/quick-start-guide-series-8.pdf?sfvrsn=0)
    * [Command Barcode Sheet](http://www.socketmobile.com/docs/default-source/series-7/chs-1d-imager-command-barcodes-sheet.pdf?sfvrsn=10)

## Warranty Checker

This sample application demonstrates how to register a device. Registration is a
two step process:

**1. Check registration status**

*Request*

        GET /v1/scanners/<bdaddress> HTTP/1.1
        Host: api.socketmobile.com:443
        Authorization: Basic [...]

*Response*

        HTTP/1.1 200 OK

        {
          "IsRegistered": false,
          "Warranty": {
            "Description": "1 Year Limited Warranty (includes 90 days buffer)",
            "EndDate": "2016-08-20",
            "ExtensionEligible": true
          },
          "Product": {
            "SerialNumber": "12345678901234567",
            "MacAddress": "00c01b112233",
            "PartNumber": "9010-01336",
            "ProductNumber": "CX2864-1336",
            "SKUNumber": "CX2864-1336",
            "Description": "CHS 7Xi, 2D Barcode Scanner, Durable, Gray",
            "UPC": "758497028648"
          }
        }

**2. Register** - If `ExtensionEligible` is true in the registration status check response, the device warranty will be extended by one year upon successful registration.

*Request*

        POST /v1/scanners/<bdaddress>/registrations
        Authorization: Basic [...]

        {
            "UserName": "",
            "UserEmail": "",
            "UserCompany": "",
            "UserAddress": "",
            "UserCity": "",
            "UserState": "",
            "UserCountry": "",
            "UserZipcode": "",
            "UserIndustry": "",
            "NumberUnits": "",
            "Purchaser": "",
            "WhrPurchased": "",
            "UseSoftscan": ""
        }

*Response*

        HTTP/1.1 200 OK

        {
          "IsRegistered": false,
          "Warranty": {
            "Description": "1 Year Limited Warranty (includes 90 days buffer)",
            "EndDate": "2017-08-20",
            "ExtensionEligible": false
          },
          "Product": {
            "SerialNumber": "12345678901234567",
            "MacAddress": "00c01b112233",
            "PartNumber": "9010-01336",
            "ProductNumber": "CX2864-1336",
            "SKUNumber": "CX2864-1336",
            "Description": "CHS 7Xi, 2D Barcode Scanner, Durable, Gray",
            "UPC": "758497028648"
          }
        }

# License

    Copyright 2015 Socket Mobile, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
