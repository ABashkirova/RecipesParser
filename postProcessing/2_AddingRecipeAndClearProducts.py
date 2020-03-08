import json
import pymorphy2

with open('./target/recipesWithCategories.json', 'r') as recipesFile:
    recipes_data = json.load(recipesFile)

with open('./postProcessing/dict-dirty-products.json', 'r') as dirtyproductsFile:
    dirtyproducts_data = json.load(dirtyproductsFile)

morph = pymorphy2.MorphAnalyzer()

units = [ 
        "ч.л. (без верха)", "ст.л. (без горки)",
        "г", "кг", "ч.л.", "ст.л.", "литр",
        "мл", "стакан", "шт.", "ст.", "ч. л.", 
        "стакана", "л", "чайная", "столовая"
    ]

products = {}
countBadIngredinets = 0
for recipe in recipes_data:
    ingredients = recipe['ingredients']
    for ingredient in ingredients:
        strWithIngredints = ingredient['ingredient']
        words = strWithIngredints.split()
        if words[0].isdigit():
            words.remove(words[0])
            if words[0] in units:
                words.remove(words[0])
        elif "–" in words[0] or "," in words[0] or "." in words[0] or "½" in words[0]:
            words.remove(words[0])
            if words[0] in units:
                words.remove(words[0])
        else:
            product = " ".join(words)
            if "(" in product:
                product = product.split(" (")[0]
            if "или" in product:
                product = product.split(" или")[0]
            products[ingredient['ingredient']] = dirtyproducts_data[product]
            continue
        dirtyProduct = ""
        for word in words:
            word = morph.parse(word)[0].normal_form
            if '(' in word or "или" in word:
                break
            else:
                dirtyProduct = dirtyProduct + " " + word
        dirtyProduct = dirtyProduct.strip()
        if dirtyProduct != "": 
            try:
                products[ingredient['ingredient']] = dirtyproducts_data[dirtyProduct]
            except KeyError:
                "Не нащди ключи!"
                print("\nНе найдена нормальная форма:\n -", dirtyProduct, ingredient['ingredient'], recipe['url'],"\n")
                products[ingredient['ingredient']] = ingredient['ingredient']
        else:
            print("\nНе найдена нормальная форма:\n -", dirtyProduct, ingredient['ingredient'], recipe['url'],"\n")
            products[ingredient['ingredient']] = ingredient['ingredient']
            countBadIngredinets += 1
print("Ошибок найдено —", countBadIngredinets)

with open('./target/dict-clear-products.json', 'w') as jsonFile:
    json.dump(products, jsonFile)
