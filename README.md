# OrientPairs
Orienteering - evaluate pair races, use data from QuickBox using PostgreSQL.

Inputs (files in the working directory):

- `secret.txt` with following lines: 
 ```
race_database_name
SQL_username
SQL_password
penalty_for_missed_control
``` 

- `pairs.csv` with following lines (repeated):
 ```
full name1,full name2,SI ID 2
```

- `categories.csv` with following lines (repeated):
 ```
category,category alias 1, ... category alias N
```
    
Outputs:

- results.csv
- resultPairs.csv
