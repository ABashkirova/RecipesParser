import json

with open('./target/categories.json', 'r') as categoriesFile:
    categories_data = json.load(categoriesFile)

with open('./target/recipes-dirty.json', 'r') as categoriesFile:
    recipes_data = json.load(categoriesFile)

for recipe in recipes_data:
    id = recipe['id']
    categoriesIds = []
    for category in categories_data:
        categoryId = category['id']
        recipeIds = category['recipeId']
        if id in recipeIds:
            categoriesIds.append(categoryId)
    recipe['categories'] = categoriesIds

with open('./target/recipesWithCategories.json', 'w') as jsonFile:
    json.dump(recipes_data, jsonFile)
