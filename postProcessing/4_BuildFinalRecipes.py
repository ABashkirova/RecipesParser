import json

with open('./target/recipesWithCategories.json', 'r') as recipesFile:
    recipes_data = json.load(recipesFile)

with open('./target/dict-ingredient-check.json', 'r') as ingredient_checkFile:
    ingredient_check_data = json.load(ingredient_checkFile)

with open('./target/dict-clear-products.json', 'r') as clear_productsFile:
    clear_products = json.load(clear_productsFile)

products = {}
counter = 1000
for (key, clear_product) in clear_products.items():
    productId = counter + 1
    products[productId] = { "id": productId, "name": clear_product}
    counter +=1

with open('./target/PRODUCTS.json', 'w') as jsonFile:
    json.dump(products, jsonFile)

for (key, ingredient) in ingredient_check_data.items():
    if ingredient['unit'] == "unknown":
        ingredient.pop('unit', None)
    if ingredient['count'] == "unknown":
        ingredient.pop('count', None)
    try:
        product = ingredient['product']
        for (pid, p) in products.items():
            if p['name'] == product:
                ingredient['productId'] = pid
                ingredient.pop('product', None)
                break
    except KeyError:
            "Не нашли ключи!"
    

for recipe in recipes_data:
    ingredients = recipe['ingredients']
    newIngredients = []
    for ingredient in ingredients:
        newIngredients.append(ingredient_check_data[ingredient['ingredient']])
    if len(recipe['ingredients']) == len(newIngredients):
        recipe['ingredients'] = newIngredients
    else:
        print("Не заполнили ", recipe['ingredients'])

with open('./target/RECIPES.json', 'w') as jsonFile:
    json.dump(recipes_data, jsonFile)