# Shopping Stats Server App - Project Notes

> **IMPORTANT**: Keep this file updated when making significant changes to the codebase. This file serves as persistent memory between Claude Code sessions.

## Overview
E-commerce product price monitoring and analytics platform. Tracks prices across shops, computes discount statistics, manages favorites/alerts, and scrapes product data via async queue. Built with Spring Boot + Vaadin + RabbitMQ.

## Key Architecture

### Core Entities

#### Product
- `id: Long`, `name: String` (3-300), `shop: String`, `productListName/Url`, `offerUrl`
- `imgSrc: LONGBLOB` (max 5MB), `categories: Set<String>`
- `attributes: OneToMany<ProductAttribute>`
- `productBasedOnDateAttributes: List<ProductBasedOnDateAttributes>` (price history, ordered DESC)

#### ProductBasedOnDateAttributes (Price History)
- `id`, `productId`, `price: BigDecimal`, `scrapDate: Date`, `formattedScrapDate: String`
- Indexed on `formattedScrapDate` and `scrapDate`

#### ActualProduct (Current Snapshot)
- `id`, `productId`, `scrapDate` (unique constraint with productId), `productName`, `shop`, `price`

#### ProductStats (Aggregated Statistics)
- `productId` (unique), `historicalLow`, `avgWholeHistory`
- `avg1Month`, `avg2Month`, `avg3Month`, `avg6Month`, `avg12Month`

#### ProductBestOffer
- `productId`, `productName`, `shop`, `price`
- `discount1Month`…`discount12Month` (discount percentages)

#### ProductAlert (User-owned)
- `name`, `priceMin/Max`, `discountMin/Max`, `productName`, `productCategories`, `emails`

#### FavoritesList / FavoritesRule
- `FavoritesList`: `listName` (unique), `disabled`
- `FavoritesRule`: `productId`, `shop`, `productName`, `priceMin/Max`, `category`, `onlyActive`
- Product name alternatives separated by semicolons

### Services

#### ProductService
- `addProductsAsync()` — bulk addition with validation and price outlier detection
- `mapProductPerDateAttributes()` — price history with deduplication (0.9% min change threshold)
- `updateStats()`, `createBestOffers()` — bulk discount metrics
- Outlier detection: rejects prices >2x average; minimum price >= 1.0

#### ActualProductService
- `flushActualProductsToDb()` — scheduled hourly (cron: `0 0 * * * *`)
- `deleteActualProducts()` — cleanup >15 days (cron: `0 15 0 * * *`)
- Uses ReentrantLock + batch processing (1000 items/batch)

#### ProductSearchService
- `findProductsByTokens()` — parallel token-based search
- `findBestOffers()` — complex discount filtering with native SQL
- Supports filtering: category, shop, price, discount (1/2/3/6/12 months), product name

#### FavoriteService
- `refreshTableForFavorites()` — materializes favorites using SQL CTE (RankedPrices)
- `onlyActive` flag restricts to products scraped within last 24 hours

#### ProductSimilarOffersService
- `createAndUpdateTokens()` — token factors: 3 (category), 2 (name), 1 (attributes)
- Converters: RomanNumeralConverter, SynonymConverter
- Delayed save with ReentrantLock

### REST API
- `POST /products/async` — add products (requires API key)
- `GET /products/categories` — all categories (CORS enabled)

### Scheduled Tasks
- Product scraping queue: every 10 min (`0 */10 * * * *`)
- Actual products flush: hourly (`0 0 * * * *`)
- Cleanup stale data: daily 00:15 (`0 15 0 * * *`)

### Views (Vaadin, via ShoppingLayout)
- Products search, Best offers, Product details, Shop config, Product config, Scrap audit, Price alerts

## Configuration
- `src/main/resources/autoconfig/`: ProductAlert.yml, ScrapAudit.yml, ShopConfig.yml, ProductConfig.yml, ProductBasedOnDateAttributes.yml
- `product-update.delayed-save` — enable/disable batch write optimization

## Important Notes
1. Dual optimization: in-memory buffers with ReentrantLock for ActualProduct and SimilarOffers
2. Price validation: 0.9% minimum change, outlier filtering (>2x average)
3. Async scraping via RabbitMQ/ActiveMQ queue
4. Favorites materialized via SQL CTE with complex rule intersection logic
5. Soft deletes on most entities; TABLE_PER_CLASS inheritance
