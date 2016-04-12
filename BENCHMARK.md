## Benchmark
Insert/Load/Clear 25000 items on Emulator, Genymotion and Nexus 7 (2013).
Benchmark tests are located at `io.techery.snapper.test.benchmark` package.

### Emulator 

+ Insert
```
|    0.000 ms | Batch Insert
| 3216.919 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   57.606 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   46.974 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   65.759 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   59.351 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   38.515 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    2.051 ms | END of Batch Insert
|
final: 3487.175 ms
```

+ Load
```
|    0.000 ms | Batch Load
| 2396.487 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   42.216 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   23.289 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   37.384 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   51.291 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|   50.593 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    2.112 ms | END of Batch Load
|
final: 2603.371 ms
```

+ Clear
```
|    0.000 ms | Batch Clear
|  926.022 ms | Storage update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 4601.738 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 3726.414 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 3499.932 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 3464.880 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 3698.451 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
|    1.578 ms | END of Batch Clear
|
final: 19919.015 ms
```

### Genymotion

+ Insert
```
|    0.000 ms | Batch Insert
| 2707.703 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  217.081 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  123.055 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  116.949 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  107.740 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  150.817 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    0.035 ms | END of Batch Insert
|
final: 3423.381 ms 
```

+ Load
```
|    0.000 ms | Batch Load
| 2287.892 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  126.722 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  147.331 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  144.558 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  118.805 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  143.937 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    0.033 ms | END of Batch Load
|
final: 2969.278 ms
```

+ Clear
```
|    0.000 ms | Batch Clear
|  771.553 ms | Storage update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 22550.293 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 22153.366 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 21780.841 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 20937.838 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 20756.431 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
|    0.034 ms | END of Batch Clear
|
final: 108950.357 ms
```

### Nexus 7 (2013)

+ Insert
```
|    0.000 ms | Batch Insert
| 7083.557 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  354.095 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  272.369 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  361.389 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  179.077 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  292.511 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    0.092 ms | END of Batch Insert
|
final: 8543.091 ms
```

+ Load
```
|    0.000 ms | Batch Load
| 7984.528 ms | Storage update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  337.952 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  296.509 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  309.448 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  320.160 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|  328.094 ms | Projection update, items count is 25000 change is StorageChange{added=25000, updated=0, removed=0}
|    0.305 ms | END of Batch Load
|
final: 9576.996 ms
```

+ Clear
```
|    0.000 ms | Batch Clear
| 2934.570 ms | Storage update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 38443.512 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 38543.610 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 37971.191 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 38011.871 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
| 38447.601 ms | Projection update, items count is 0 change is StorageChange{added=0, updated=0, removed=25000}
|    0.092 ms | END of Batch Clear
|
final: 194352.448 ms
```
