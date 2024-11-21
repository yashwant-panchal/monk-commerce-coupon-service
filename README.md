Monk Commerce Coupon Service
==========

## Prerequisite to run the service : 
1. Run time version should be > Java(17)
2. Run the service using : ``./gradlew bootRun``
3. If this file is missing : [src/main/resources/db/couponDB/COUPON_DB.mv.db](src/main/resources/db/couponDB/COUPON_DB.mv.db)
      * Then Run the service.
      * Log into the H2-Console : http://localhost:8080/h2-console
      * JDBC Url : `jdbc:h2:file:./src/main/resources/db/couponDB/COUPON_DB`
      * Username = sa
      * Password = 
      * Create COUPON table using this SQL statement :
        * ```roomsql
          CREATE TABLE COUPON (
               id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,  
               coupon_type VARCHAR(255) NOT NULL,           
               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
               updated_at TIMESTAMP,                       
               expires_at TIMESTAMP,                      
               discount_percentage INT,  
               conditions JSON,                            
               description VARCHAR(255),
               CONSTRAINT CHK_EXPIRES_GT_CREATED CHECK (EXPIRES_AT > CREATED_AT)
           );
          ```

## Implemented APIS 
* **POST /coupons**: Create a new coupon.
* **GET /coupons**: Retrieve all coupons.
* **GET /coupons/{id}**: Retrieve a specific coupon by its ID.
* **PUT /coupons/{id}**: Update a specific coupon by its ID.
* **DELETE /coupons/{id}**: Delete a specific coupon by its ID.
* **POST /applicable-coupons**: Fetch all applicable coupons for a given cart and calculate the total discount that will be applied by each coupon.
* **POST /apply-coupon/{id}**: Apply a specific coupon to the cart and return the updated cart with discounted prices for each item.

## Implemented Coupon Types : 
* **CART_WISE** : Apply a discount to the entire cart if the total amount exceeds a
certain threshold.
* **PRODUCT_WISE** : Apply a discount to specific products.
* **BX_GY** : “Buy X, Get Y” deals with a repetition limit and can be applicable to a set of products (e.g., Buy 3 of Product X or Product Y, get 1 of Product P and Product Q free and so on).

## Coupon Constraints
Applied various constraints for creating coupons :
* Common Constrains applied on each coupon :
  * Each coupon should have expiry date after the created date of coupon.
1. For Coupon type : **CART_WISE**
   1. This type of coupon must contain these properties in condition hashmap : 
      * Should has a ``minValueToApplyDiscount`` which is minimum value of cart to apply this coupon.
   2. Should has ``discount_percentage`` for applying the discount on cart.


2. For Coupon type : **PRODUCT_WISE**
   1. This type of coupon must contain these properties in condition hashmap :
        * Should has a product array on which the coupon will be applied.
   2. Should has ``discount_percentage`` for applying the discount on cart.


3. For Coupon type : **BX_GY**
    1. This type of coupon must contain these properties in condition hashmap :
       * Should has ``buy_product`` object with these values : 
         * name : name of the product that needed to be purchased
         * quantity : minimum quantity that need to be purchased
       * Should has ``get_product`` object with these values :
           * name : name of the product that will be available free
           * quantity : Quantity of this product that will be available free
       * Should has ``repetition_limit`` property as mentioned in the doc 
       * Example 
         * ```json
            {
                "coupon_type": "BX_GY",
                "conditions": {
                    "buy_product": {
                        "name": "A",
                        "quantity": 3
                    },
                    "get_product": {
                        "name": "B",
                        "quantity": 1
                },
                
                "repetition_limit": 2
                },
                "expires_at": "2024-12-21T05:41:44.811"
            }
           ```


## Database
* Used h2 file database to store the COUPON and conditions.
* Database is present at this path : ``src/main/resources/db/couponDB/COUPON_DB.mv.db``
* Only Coupon is stored in the Database. Cart is not stored.

## Error Handling
* Implemented error handling when certain constrains failed.
* Examples : 
  * When a constraint is failed for BX_GY type of coupon : 
    * ```json
          {
              "details": "Please provide following properties\n1. buy_products : List of products that needed to be purchased by customer\n2. get_products : List of products that will be provided free to customer\n3. min_buy : Min number of product that should be purchased from buy_product list\n4. get_free : Number of product that will be provided free to customer\n5. repetition_limit : How many items can be avail free.\n  - E.g. If the repetition limit is 3, the coupon can be applied 3 times.\n       - If the cart has 6 products from the “buy_products” list and 3 products from the\n       “get_products” list, the coupon can be applied 3 times. I.e. for b2g1, buying 6\n       products from [X, Y, Z] would result in getting 3 products from [A, B, C] for\n       free.\n       - If the cart has products [X, X, X, Y, Y, Y] (6 items from the “buy” array)\n       and products [A, B, C] or [A,B], then [A, B, and C] or [A,B] would be free.\n",
              "message": "Bad Request"
          }
      ```
  * When expiry date is before created date : 
    * ```json
        {
            "details": "expiry date should be greater than created_date",
            "message": "BAD REQUEST"
        }
      ```
  * When coupon is not found while applying on cart
    * ```json
        {
            "details": "Invalid COUPON Id : 56b6a7dd-0b0a-499d-8ce9-e66b8c2014aa, Coupon not found!",
            "message": "Bad Request"
        }
      ```
  * When coupon is not applicable on cart : 
    * ```json
        {
            "details": "This coupon is not applicable on cart!!!",
            "message": "Bad Request"
        }
      ```
   

## Example of APIS Requests :
* `POST /coupons` : Create a new coupon.
  * Example request : 
    * ```commandline
        curl --location 'localhost:8080/coupons' \
            --header 'Content-Type: application/json' \
            --data 
            '{
                "coupon_type": "CART_WISE",
                "conditions": {
                    "minValueToApplyDiscount": 1000.00
                },
                "expires_at": "2024-12-21T05:41:44.811",
                "discount_percentage": 15
            }'
      ```
      * Response : 
        * ```json lines
            {
                "id": "e93f9a1e-8e40-402b-89c5-7e0ec92726ce",
                "coupon_type": "CART_WISE",
                "created_at": 1732179150165,
                "updated_at": 1732179150165,
                "expires_at": 1734759704811,
                "discount_percentage": 15,
                "conditions": {
                  "minValueToApplyDiscount": 1000.0
                },
                "description": "Discount Coupon"
            }
          ```

* `GET /coupons` : Retrieve all coupons.
  * Example Request : 
    * ```commandline
         curl --location 'localhost:8080/coupons'   
      ```
    * Response
      * ```json
          [
              {
                  "id": "56b6a7dd-0b0a-499d-8ce9-e66b8c2014aa",
                  "coupon_type": "BX_GY",
                  "created_at": 1732178500385,
                  "updated_at": 1732178500385,
                  "expires_at": 1734759704811,
                  "discount_percentage": 0,
                  "conditions": {
                      "buy_product": {
                          "name": "A",
                          "quantity": 3
                      },
                      "get_product": {
                          "name": "B",
                          "quantity": 1
                      },
                      "repetition_limit": 2
                  },
                  "description": "Discount Coupon"
              },
              {
                  "id": "e93f9a1e-8e40-402b-89c5-7e0ec92726ce",
                  "coupon_type": "CART_WISE",
                  "created_at": 1732179150165,
                  "updated_at": 1732179150165,
                  "expires_at": 1734759704811,
                  "discount_percentage": 15,
                  "conditions": {
                    "minValueToApplyDiscount": 1000.0
                  },
                  "description": "Discount Coupon"
              },
              {
                  "id": "1b2f26ff-6b27-40da-a521-a7e93f186255",
                  "coupon_type": "PRODUCT_WISE",
                  "created_at": 1732179164383,
                  "updated_at": 1732179164383,
                  "expires_at": 1734759704811,
                  "discount_percentage": 10,
                  "conditions": {
                    "product": "A"
                  },
                  "description": "Discount Coupon"
              }
          ]
        ```

* `GET /coupons/{id}` : Retrieve a specific coupon by its ID.
  * Example Request
    * ```commandline
        curl --location 'localhost:8080/coupons/43e19bb5-e3fb-4fc3-99d7-19b2e800d2bd' --data ''
      ```
  * Response
      * ```json lines
           {
              "id": "e93f9a1e-8e40-402b-89c5-7e0ec92726ce",
              "coupon_type": "CART_WISE",
              "created_at": 1732179150165,
              "updated_at": 1732179150165,
              "expires_at": 1734759704811,
              "discount_percentage": 15,
              "conditions": {
                "minValueToApplyDiscount": 1000.0
              },
              "description": "Discount Coupon"
           }
         ```
* `PUT /coupons/{id}` : Update a specific coupon by its ID.
  * Example Request : 
    * ```commandline
        curl --location --request PUT 'localhost:8080/coupons/b34c2dd8-3952-497f-8389-ef1a311f1ca4' \
            --header 'Content-Type: application/json' \
            --data '{
            "coupon_type": "BX_GY",
            "conditions": {
                 "buy_product": {
                    "name": "A",
                    "qunatity": 3    
                },
                "get_product": {
                    "name": "B",
                    "quantity": 1
                },
                "repetition_limit": 3
            },
            "expires_at": "2024-12-21T05:41:44.811",
            "description": "Buy 3 A, get 1 B free"
         }'
      ```
  * Response : 
    * ```json
        {
            "id": "e86d3ef6-73ec-41b1-9e46-0457cc96722b",
            "coupon_type": "BX_GY",
            "created_at": 1732183229236,
            "updated_at": 1732183229236,
            "expires_at": 1734759704811,
            "discount_percentage": 0,
            "conditions": {
                "buy_product": {
                    "name": "A",
                    "quantity": 3
                },
                "get_product": {
                    "name": "B",
                    "quantity": 1
                },
                "repetition_limit": 3
            },
            "description": "Buy 3 A, get 1 B free"
        }
      ```
* `DELETE /coupons/{id}` : Delete a specific coupon by its ID.
  * Example Request : 
    * ```commandline
        curl --location --request DELETE 'localhost:8080/coupons/43e19bb5-e3fb-4fc3-99d7-19b2e800d2bd' \
        --data ''
      ```
  * Response 
    * ```commandline
        DELETION SUCCESS
      ```
* `POST /applicable-coupons` : Fetch all applicable coupons for a given cart and calculate the total discount that will be applied by each coupon.
  * Example Request : 
    * ```commandline
        curl --location 'localhost:8080/applicable-coupons' \
            --header 'Content-Type: application/json' \
            --data '{
            "products": [
                {
                    "name": "A",
                    "quantity": 6,
                    "priceOfSingleItem": 200.0
                },
                {
                    "name": "B",
                    "quantity": 3,
                    "priceOfSingleItem": 100.0
                }
             ]
          }'
      ```
  * Response : 
    * ```json
      [
        {
            "total discount available": 200.0,
            "Coupon": {
                "id": "56b6a7dd-0b0a-499d-8ce9-e66b8c2014aa",
                "coupon_type": "BX_GY",
                "created_at": 1732178500385,
                "updated_at": 1732178500385,
                "expires_at": 1734759704811,
                "discount_percentage": 0,
                "conditions": {
                    "buy_product": {
                        "name": "A",
                        "quantity": 3
                    },
                    "get_product": {
                        "name": "B",
                        "quantity": 1
                    },
                    "repetition_limit": 2
                },
                "description": "Discount Coupon"
            }
        },
        {
            "total discount available": 225.0,
            "Coupon": {
                "id": "e93f9a1e-8e40-402b-89c5-7e0ec92726ce",
                "coupon_type": "CART_WISE",
                "created_at": 1732179150165,
                "updated_at": 1732179150165,
                "expires_at": 1734759704811,
                "discount_percentage": 15,
                "conditions": {
                    "minValueToApplyDiscount": 1000.0
                },
                "description": "Discount Coupon"
            }
        },
        {
            "total discount available": 120.0,
            "Coupon": {
                "id": "1b2f26ff-6b27-40da-a521-a7e93f186255",
                "coupon_type": "PRODUCT_WISE",
                "created_at": 1732179164383,
                "updated_at": 1732179164383,
                "expires_at": 1734759704811,
                "discount_percentage": 10,
                "conditions": {
                    "product": "A"
                },
                "description": "Discount Coupon"
            }
        }
      ]
      ```
* `POST /apply-coupon/{id}` : Apply a specific coupon to the cart and return the updated cart with discounted prices for each item.
* Example Request : 
  * ```commandline
    curl --location 'localhost:8080/apply-coupon/56b6a7dd-0b0a-499d-8ce9-e66b8c2014aa' \
        --header 'Content-Type: application/json' \
        --data '{
        "products": [
            {
                "name": "A",
                "quantity": 6,
                "priceOfSingleItem": 100.0
            },
            {
                "name": "B",
                "quantity": 3,
                "priceOfSingleItem": 50.0
            }
        ]
      }'
    ```
  * Response :
    * ```json
        {
            "products": [
                {
                    "name": "A",
                    "quantity": 6,
                    "priceOfSingleItem": 100.0,
                    "finalPriceOfSingleItemAfterDiscount": 100.0
                },
                {
                    "name": "B",
                    "quantity": 3,
                    "priceOfSingleItem": 50.0,
                    "finalPriceOfSingleItemAfterDiscount": 16.666666666666668
                }
            ],
            "originalAmountOfCart": 750.0,
            "availableDiscount": 100.0,
            "finalAmountOfCartAfterDiscount": 650.0
      }
      ```
    
# Test the service : 
Import [postManCollection.json](MonCommerceAPIs.postman_collection.json) in postman and test the service.