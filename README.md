# Parcel Delivery System

A simple console-based Parcel Delivery System in Java.

## Concepts demonstrated
- **Polymorphism**: `Parcel` (abstract) -> `Document`, `Box`, `Fragile` (extends `Box`). Each overrides `getChargeableWeight()` / `getBaseFee()` / `getType()`.
- **Exception handling**: `InvalidInputException`, `DuplicateParcelException`, `ParcelNotFoundException`, plus handling of bad number input.
- **Singleton pattern**: `ParcelManager` (one shared parcel list, accessed via `getInstance()`).
- **Strategy pattern**: `DeliveryService` interface with `StandardService`, `ExpressService`, `SameDayService`.

## How to run
```
javac -d out src/*.java
java -cp out Main
```
