# spanish-cities-bot

This project helps to fill [spanish-cities](https://github.com/ByMykel/spanish-cities) with data.

It takes `json` crawled by [wiki-image-crawler](https://github.com/AloisSeckar/wiki-image-crawler) and tries to apply it on `cities.json` file from [spanish-cities](https://github.com/ByMykel/spanish-cities). 

## How to use?
1. Pick a [Spanish province](https://es.wikipedia.org/wiki/Provincia_(Espa%C3%B1a))
2. Run [wiki-image-crawler](https://github.com/AloisSeckar/wiki-image-crawler) on [flag category page](https://commons.wikimedia.org/wiki/Category:SVG_flags_of_municipalities_of_Spain_by_province) of selected province to produce `flags` file (you can name the output as you see fit)
3. Run [wiki-image-crawler](https://github.com/AloisSeckar/wiki-image-crawler) on [coat of arms category page](https://commons.wikimedia.org/wiki/Category:SVG_coats_of_arms_of_municipalities_of_Spain_by_province) of selected province to produce `coat-of-arms` file (you can name the output as you see fit)
4. Configure **spanish-cities-bot** (`bot.config` file in project directory) to read from those files and to write into correct `cities` file (preferably directly in [spanish-cities](https://github.com/ByMykel/spanish-cities) project directory)
7. Sync fork of [spanish-cities](https://github.com/ByMykel/spanish-cities) to get the latest source data
8. Run **spanish-cities-bot** to apply `flags` and `coat-of-arms` files on `cities` data
9. Open updated `cities.json` in IDE with highlighted changes
10. Go through cities of current province (search all `"code_province": "<number>"`)
     1. Check if changes are reasonable (sometimes the bot matches wrong city name)
     2. If there is still `null` value, check at [spanish Wikipedia](https://es.wikipedia.org/) whether image is available (sometimes bot cannot grab the correct image or cannot find the matching city). **Attention:** If the image is not `.svg` (bot ignores such), don't use it.
