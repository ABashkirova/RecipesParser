import json
import pymorphy2

with open('./target/recipesWithCategories.json', 'r') as recipesFile:
    recipes_data = json.load(recipesFile)

with open('./target/dict-clear-products.json', 'r') as clear_productsFile:
    clear_products_data = json.load(clear_productsFile)
morph = pymorphy2.MorphAnalyzer()

units = [ 
            "литр ", "литра ", "баночка ", "баночки ", "палочка ",
            "стакан ", "стакана ",  "чайная ложка ", "столовая ложка ",
            "чайные ложки ", "столовые ложки ", "головка ", "зубчик ", "зубчика ",
            "горошин ", "зернышка ", "бутончика ", "кусочка ", "полосок ", "зубчиков ",  "пучок ",
            "веточки ", "пучков ", "кубиков ", "лепешки ", 
            "кг ", "г ", "мл ", "л ", "шт. ", 
            "ч.л. ", "ст.л. ", "ч. л. ", "ст. л. "
        ]
unitsProduct = [
            "г", "кг", "ч.л.", "ст.л.", "литр",
            "мл", "стакан", "шт.", "ст.", "ч. л.", 
            "стакана", "л", "чайная", "столовая"
        ]

full_ingredients = {}
for recipe in recipes_data:
    ingredients = recipe['ingredients']
    
    for ingredient in ingredients:
        dict_ingredient = {}
        strWithIngredints = ingredient['ingredient']
        
        dict_ingredient['description'] = strWithIngredints
        dict_ingredient['count'] = "unknown"
        dict_ingredient['unit'] = "unknown"
        # unit and count
        for unit in units:
            if unit in strWithIngredints: 
                dict_ingredient['unit'] = unit.strip()
                dict_ingredient['count'] = strWithIngredints.split()[0]
                break
        
        # product
        words = strWithIngredints.split()
        if words[0].isdigit() or "–" in words[0]:
            if dict_ingredient['count'] == "unknown":
                dict_ingredient['count'] = words[0]
                dict_ingredient['unit'] = "шт."
        try:
            dict_ingredient['product'] = clear_products_data[ingredient['ingredient']]
        except KeyError:
            "Не нащди ключи!"
        # полный json
        full_ingredients[ingredient['ingredient']] = dict_ingredient


with open('./target/dict-ingredient-check.json', 'w') as jsonFile:
    json.dump(full_ingredients, jsonFile)