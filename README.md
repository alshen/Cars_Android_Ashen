Reuiqments:
Android Studio Beta (0.8.9) - does not appear backwards compatible with Eclipse
Android Test Device (v4.0.3+) - wifi enabled (I used Motorola Razer V (xt855)) - (anything with at least honeycomb should be ok)

Instructions:

1) git clone https://github.com/alshen/Cars_Android_Ashen.git
2) open Android Studio
3) under File->import project, navigate to project directory, click ok
4) under Build->Make project
5) attach android device to computer
6) under Run->run app
7) select device to run on

Features:
* JSON - (via http get)
  - Avaiable Cars Listings are queried from JSON API
  - Standard Prices to Car Listings are queried from JSON API
  - Best/Worst can also be retrieved from JSON API (not used atm)
* SQLITE
  - Car Listings are cached in database
  - Car Listing rankings are stored based on value (asking price - standard price)
  - Listings can be inserted/updated/retreived
* Starred/Viewed
  - You can star listings and they will be stored in the database
* Images
  - Images are loaded using the Universal Image Loader
* Search
  - Car Listings can be queried for based on a range of criteria
* Detailed View
  - Clicked a Car Listing will bring up a detailed view
