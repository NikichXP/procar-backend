# Auction Provider Procar Module Implementation Plan (Simplified)

## Overview
This plan outlines the implementation of the `auction-provider-procar` module, an in-house auction provider that will be the first provider to implement the `auction-provider-api` contract. The module will be a **child module of procar-backend** and will have a **dependency on auction-provider-api**. The module will use MongoDB as the database and is designed for internal use only, selling Procar's own stock.

## Technology Stack
- **Framework**: Spring Boot with Spring Data MongoDB
- **Database**: MongoDB for document storage
- **Testing**: Out of scope for this iteration
- **Documentation**: Internal only (no Swagger/OpenAPI)

## High Priority Tasks

### 1. Create auction-provider-procar module structure with Gradle build configuration (MongoDB)
- Set up standard Spring Boot module structure as a **child module of procar-backend**
- Configure dependencies:
  - Spring Boot Web
  - Spring Data MongoDB
  - Validation framework
  - **auction-provider-api as implementation dependency**
- Configure MongoDB connection settings

### 2. Implement InternalLotAPI interface with in-house auction logic
- Create REST API class implementing lot operations (internal use only)
- Implement lot search with advanced filtering and pagination
- Handle lot detail retrieval with full information
- Simple lot monitoring (no real-time, frontend will poll)
- MongoDB-based lot operations

### 3. Implement InternalBidAPI interface with in-house bidding logic
- Create REST API class for bid operations
- Implement bid history retrieval with pagination
- Handle bid placement with validation and winner determination
- Implement bid validation with business rules
- Simple bid analytics (no aggregation pipelines)
- MongoDB-based bid management

### 4. Create MongoDB document models for lots, bids, and related collections
- Design `@Document` classes:
  - `LotDocument` - auction lots with metadata
  - `BidDocument` - bid history and details
  - `AuctionEventDocument` - auction lifecycle events
- Design embedded documents for related data structures
- Implement MongoDB schema validation

### 5. Create service layer for business logic implementation
- **LotService**:
  - Lot creation and management
  - Search and filtering logic
  - Status transitions and validation
- **BidService**:
  - Bid placement and validation
  - Concurrent bidding handling
  - Winner determination logic
- MongoDB operations integration

### 6. Update settings.gradle.kts to include new auction-provider-procar module
- Add `auction-provider-procar` to multi-project configuration as a **child module of procar-backend**
- Configure module dependencies including **dependency on auction-provider-api**
- Set up proper module hierarchy

## Medium Priority Tasks

### 7. Implement MongoDB repository layer for data access
- Spring Data MongoDB repositories:
  - `LotRepository` with custom queries
  - `BidRepository` with basic operations
  - `AuctionEventRepository` for event tracking
- Custom queries using `@Query` annotation
- MongoTemplate for complex operations
- Pagination and sorting support

### 8. Implement lot search functionality with MongoDB text search and filtering
- MongoDB text indexes for full-text search capabilities
- Geospatial queries for location-based filtering
- Basic filtering (no complex aggregation)
- Search relevance scoring and suggestions

### 9. Implement bid validation and placement logic
- Bid validation rules and business logic
- Concurrent bid handling with atomic operations
- Winner determination algorithms
- Bid increment validation
- Minimum bid enforcement

### 10. Create configuration properties for auction settings
- MongoDB connection and configuration settings
- Auction-specific configuration:
  - Bid increments and minimum bids
  - Auction timeouts and extensions
  - Reserve price settings
- Provider-specific configuration
- Environment-based settings management

## MongoDB-Specific Considerations

### Document Design Strategy
- Optimize document structure for common query patterns
- Balance between embedding and referencing based on access patterns
- Use document modeling for hierarchical data (bids within lots)
- Consider data duplication for read performance

### Indexing Strategy
- Text indexes for full-text search functionality (basic setup)
- Geospatial indexes for location-based queries
- Index creation deferred to later iteration

### Performance Optimization
- Implement proper connection pooling
- Configure read/write concerns appropriately
- Use projection to limit data transfer
- Implement caching for frequently accessed data

### Concurrency and Consistency
- Handle concurrent bids with atomic operations
- Use MongoDB transactions for multi-document operations
- Implement optimistic locking for bid placement
- Handle write conflicts gracefully
- Ensure data consistency across operations

## Module Structure
```
procar-backend/
├── auction-provider-procar/  (child module)
│   ├── build.gradle.kts       (with auction-provider-api dependency)
│   └── src/main/kotlin/com/procar/auction/
│       ├── config/
│       │   ├── MongoDBConfig.kt
│       │   └── AuctionProperties.kt
│       ├── api/
│       │   ├── LotAPI.kt
│       │   └── BidAPI.kt
│       ├── document/
│       │   ├── LotDocument.kt
│       │   ├── BidDocument.kt
│       │   └── AuctionEventDocument.kt
│       ├── repository/
│       │   ├── LotRepository.kt
│       │   ├── BidRepository.kt
│       │   └── AuctionEventRepository.kt
│       ├── service/
│       │   ├── LotService.kt
│       │   └── BidService.kt
│       └── AuctionProviderProcarApplication.kt
└── auction-provider-api/     (dependency module)
```

## Implementation Phases

### Phase 1: Foundation (High Priority)
1. Module setup and configuration
2. MongoDB document models
3. Basic repository layer
4. Core service implementations
5. API controllers

### Phase 2: Advanced Features (Medium Priority)
1. Search functionality
2. Advanced service logic
3. Configuration management
4. Performance optimization

## Success Criteria
- Full implementation of `auction-provider-api` contract
- MongoDB-based data persistence with optimal performance
- Simple lot monitoring (polling-based)
- Production-ready configuration and deployment

## Risks and Mitigations
- **MongoDB Performance**: Proper indexing and query optimization
- **Concurrent Bidding**: Atomic operations and proper locking
- **Data Consistency**: Proper transaction usage and validation
- **Scalability**: Document design optimized for growth
