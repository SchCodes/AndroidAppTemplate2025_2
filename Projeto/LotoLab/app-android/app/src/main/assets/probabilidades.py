def calcular_probabilidade(numeros_selecionados):
    if not numeros_selecionados:
        return 0
    resultado = sum(numeros_selecionados)/len(numeros_selecionados)
    return round(resultado,2)