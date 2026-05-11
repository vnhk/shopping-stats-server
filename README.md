# shopping-stats-server-app

E-commerce product price monitoring and analytics platform. Tracks prices across shops, computes discount statistics, manages favorites and alerts, and scrapes product data via an async queue.

## Features

- **Price history**: Tracks price changes per product (min 0.9% change threshold, outlier filtering)
- **Discount analytics**: Historical low, average, and discount % over 1/2/3/6/12 months
- **Best offers**: Ranked by discount across shops and categories
- **Price alerts**: User-defined rules (product name, category, price/discount range) with email notifications
- **Favorites**: Materialized via SQL CTE with complex rule intersection logic
- **Async scraping**: RabbitMQ/ActiveMQ queue, every 10 minutes
- **Similar offers**: Token-based product similarity (category × 3, name × 2, attributes × 1)

## Key Entities

| Entity | Description |
|--------|-------------|
| `Product` | Product with shop, URL, image (LONGBLOB ≤ 5 MB), categories, attributes |
| `ProductBasedOnDateAttributes` | Price history entry with date (indexed) |
| `ActualProduct` | Current snapshot (flushed hourly, cleaned after 15 days) |
| `ProductStats` | Aggregated statistics: historical low, averages |
| `ProductBestOffer` | Best price with discount percentages |
| `ProductAlert` | User-owned price/discount alert with email list |
| `FavoritesList` / `FavoritesRule` | Saved product filter rules |

## REST API

| Endpoint | Description |
|----------|-------------|
| `POST /products/async` | Add products (requires API key) |
| `GET /products/categories` | All categories (CORS enabled) |
| `GET /api/shopping/*` | Products search, best-offers, alerts, shop/product configs, scrap audits |

## Scheduled Tasks

| Schedule | Task |
|----------|------|
| Every 10 min | Process scraping queue |
| Every hour | Flush actual product snapshots to DB |
| Daily 00:15 | Clean up stale actual products (>15 days) |

## Price Validation

- Minimum price: ≥ 1.0
- Outlier rejection: price > 2× average is discarded
- Deduplication: changes < 0.9% are ignored

## Build

```bash
mvn clean install -DskipTests
```

Part of the `my-tools` multi-module Maven project. Requires `common` to be built first. External scraper lives in `profile-stealth/`.
