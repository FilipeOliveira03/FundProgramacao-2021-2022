val esc: String = "\u001B"
val legendaNumeros = "123456789"
val separacao = "| "
val coordsAnteriores = Pair(0,0)

fun makeMenu(): String{         //Cria a string do menu principal
    return  "\n" +
            "Welcome to DEISI Minesweeper\n" +
            "\n" +
            "1 - Start New Game\n" +
            "0 - Exit Game\n"
}

fun makeTerrain(matrixTerrain: Array<Array<Pair<String, Boolean>>>, showLegend: Boolean = true,
                withColor: Boolean = false, showEverything: Boolean = false): String{
    var terreno = ""
    var legendColor = ""
    var endLegendColor = ""

    if(withColor){
        legendColor = "$esc[97;44m"
        endLegendColor = "$esc[0m"
    }
    val cor = "$legendColor   $endLegendColor"

    if(showLegend) terreno += "$legendColor    " + createLegend(matrixTerrain[0].size) + "    $endLegendColor" + "\n"

    for(linha in 0..matrixTerrain.size - 1){

        if(linha <= matrixTerrain.size - 1 && showLegend){
            val posicao = legendaNumeros[linha]
            terreno += "$legendColor $posicao $endLegendColor"
        }

        if(!showLegend && linha != 0) terreno += "\n"
        terreno += " "

        for (coluna in 0..matrixTerrain[0].size - 1){
            var posicao = matrixTerrain[linha][coluna].first

            if(!showEverything){        // isto é quando for tudo escondido no tabuleiro, para não se ver as minas e números possiveis a volta
                if(!matrixTerrain[linha][coluna].second) { posicao = " " }
            }

            terreno += posicao + " "

            if(coluna < matrixTerrain[0].size - 1) terreno += separacao

            if(coluna == matrixTerrain[0].size - 1 && showLegend) terreno += cor
        }

        if(linha <= matrixTerrain.size - 2){
            if(showLegend){
                terreno += "\n" + cor + "---+---"
            } else terreno += "\n" + "---+---"

            var count = 0
            while(count < matrixTerrain[0].size - 2){
                terreno += "+---"
                count++
            }

            if(count < matrixTerrain[0].size - 1 && showLegend) terreno += cor + "\n"

        }else if(linha == matrixTerrain.size - 1 && showLegend){

            terreno += "\n" + "$legendColor     "

            var count = 0
            while(count < matrixTerrain[0].size){
                terreno += "    "
                count++
            }

            terreno += "$endLegendColor"
        }
    }
    return terreno
}

fun isNameValid(name: String?, minLength: Int = 3): Boolean{
// Valida o nome, em que a primeira letra é maiuscula, a primeira do segundo nome tmb e se o primeiro nome tem 3 caracteres

    val tamanho = name!!.length
    val caracter = name[0]
    var espaco = 0
    var count = 0
    if(name != null){
        do{
            if(name[count] == ' '){
                espaco = count
            }
            count++
        }while(tamanho != count)
    }
    return (tamanho > minLength && caracter.isUpperCase() && name[espaco + 1].isUpperCase() && name != null)
}

fun calculateNumMinesForGameConfiguration(numLines: Int, numColumns: Int): Int?{
// Calcula o número de minas dependo dos espaços em branco, no caso de o utilizador clicar enter

    return when (numLines * numColumns - 2){
        in 14..20 -> 6
        in 21..40 -> 9
        in 41..60 -> 12
        in 61..79 -> 19
        else -> null
    }
}

fun createLegend(numColumns: Int): String{
// Cria a legenda das letras, em cima do tabuleiro

    val abecedario = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    var count = 0
    var legend = ""
    do{
        legend += abecedario[count]

        if(count <= numColumns - 2){
            legend += "   "
        }

        count++
    } while (count <= numColumns - 1)
    return legend
}

fun isValidGameMinesConfiguration(numLines: Int, numColumns: Int, numMines: Int): Boolean{
// Valida se o número de minas introduzidas pelo utilizador é correta e possivel

    val casasVazias = numLines * numColumns - 2
    return when{
        numMines <= 0 -> false
        casasVazias < numMines -> false
        else -> true
    }
}

fun createMatrixTerrain(numLines: Int, numColumns: Int, numMines: Int, ensurePathToWin: Boolean = false): Array<Array<Pair<String, Boolean>>> {
    // vai criar a matriz com as minas, casa de partida com o jogador e casa final

    val matrix = Array(numLines) { Array(numColumns) {Pair(" ",false)} }

    matrix [0][0] = Pair("P", true)
    matrix [numLines - 1][numColumns - 1] = Pair("f", true)

    if(!ensurePathToWin){ // no caso de ser no modo totalmente aleatório

        var linhaRandom = (1 until numLines).random()
        var colunaRandom = (0 until numColumns).random()
        var count = 0

        while(count < numMines){
            if((linhaRandom == 0 && colunaRandom == 0)){
                matrix[linhaRandom][colunaRandom] = Pair("P",true)
            } else if(linhaRandom == numLines - 1 && colunaRandom == numColumns - 1){
                matrix[linhaRandom][colunaRandom] = Pair("f",true)
            } else if(matrix[linhaRandom][colunaRandom] == Pair("*", false)){
                matrix[linhaRandom][colunaRandom] = Pair("*",false)
            } else{
                matrix[linhaRandom][colunaRandom] = Pair("*",false)
                count++
            }

            linhaRandom = (1 until numLines).random()
            colunaRandom = (0 until numColumns).random()
        }

    } else{ // no caso de ser no modo em que não pode ter nenhuma mina uma ao lado da outra, num raio de um quadrado a volta dessa mina

        var linhaRandom = (0 until numLines).random()
        var colunaRandom = (0 until numColumns).random()
        var count = 0

        while(count < numMines){

            if((linhaRandom == 0 && colunaRandom == 0)){
                matrix[linhaRandom][colunaRandom] = Pair("P",true)
            } else if(linhaRandom == numLines - 1 && colunaRandom == numColumns - 1){
                matrix[linhaRandom][colunaRandom] = Pair("f",true)
            } else if(matrix[linhaRandom][colunaRandom] == Pair("*", false)){
                matrix[linhaRandom][colunaRandom] = Pair("*",false)
            } else{

                val quadrado = getSquareAroundPoint(linhaRandom, colunaRandom, numLines - 1, numColumns - 1)
                val primPair = quadrado.first
                val segPair = quadrado.second
                val verificar = isEmptyAround(matrix, linhaRandom, colunaRandom, primPair.first, primPair.second, segPair.first, segPair.second)
                // primPair.first(yL) = y-1 ; primPair.second(xL) = x-1 ; segPair.first(yR) = y+1 ; segPair.second(xR) = x+1

                if(verificar){
                    matrix[linhaRandom][colunaRandom] = Pair("*",false)
                    count++
                }

            }

            linhaRandom = (0 until numLines).random()
            colunaRandom = (0 until numColumns).random()

        }
    }
    return matrix
}

fun countNumberOfMinesCloseToCurrentCell(matrixTerrain: Array<Array<Pair<String, Boolean>>>, centerY: Int, centerX: Int): Int{
    // vai contar o número de minas a volta de cada célula para poder escrever os números que estejam perto das minas

    var count = 0
    val quadrado = getSquareAroundPoint(centerY, centerX, matrixTerrain.size - 1, matrixTerrain[0].size - 1)
    val primPrimPair = (quadrado.first).first   // primPair.first(yL) = y-1
    val segPrimPair = (quadrado.second).first   // segPair.first(yR) = y+1
    val primSegPair = (quadrado.first).second   // primPair.second(xL) = x-1
    val segSegPair = (quadrado.second).second   // segPair.second(xR) = x+1

    if(centerY == segPrimPair || centerX == segSegPair || centerX == primSegPair || centerY == primPrimPair){
        // Quando se encontra numa das pontas, ou quando se encontra encontado a linha ou coluna, tanto no 0 ou ...-1

        if((matrixTerrain[primPrimPair][primSegPair].first == "*") && (centerY == segPrimPair || centerX == segSegPair)){
            count++
        }
        // No caso de estar em cima esquerda e

        if((matrixTerrain[primPrimPair][centerX].first == "*") && (centerY == segPrimPair || centerX == segSegPair || centerX == primSegPair)){
            count++
        }
        // No caso de estar meio esquerda e

        if((matrixTerrain[primPrimPair][segSegPair].first == "*") && (centerY == primPrimPair || centerX == primSegPair)){
            count++
        }
        // No caso de estar baixo esquerda e

        if((matrixTerrain[centerY][primSegPair].first == "*") && (centerY == primPrimPair || centerY == segPrimPair || centerX == segSegPair)){
            count++
        }
        // No caso de estar cima meio e

        if((matrixTerrain[centerY][segSegPair].first == "*") && (centerY == segPrimPair || centerY == primPrimPair || centerX == primSegPair)){
            count++
        }
        // No caso de estar baixo meio e

        if((matrixTerrain[segPrimPair][primSegPair].first == "*") && (centerY == primPrimPair || centerX == segSegPair)){
            count++
        }
        // No caso de estar cima direita e

        if((matrixTerrain[segPrimPair][centerX].first == "*") && (centerY == primPrimPair || centerX == segSegPair || centerX == primSegPair)){
            count++
        }
        // No caso de estar meio direita e

        if((matrixTerrain[segPrimPair][segSegPair].first == "*") && (centerY == primPrimPair || centerX == primSegPair)){
            count++
        }
        // No caso de estar baixo direita e

    } else{

        if(matrixTerrain[primPrimPair][primSegPair].first == "*"){
            count++
        }
        // No caso de estar em cima esquerda

        if(matrixTerrain[primPrimPair][centerX].first == "*"){
            count++
        }
        // No caso de estar meio esquerda

        if(matrixTerrain[primPrimPair][segSegPair].first == "*"){
            count++
        }
        // No caso de estar baixo esquerda

        if(matrixTerrain[centerY][primSegPair].first == "*"){
            count++
        }
        // No caso de estar cima meio

        if(matrixTerrain[centerY][segSegPair].first == "*"){
            count++
        }
        // No caso de estar baixo meio

        if(matrixTerrain[segPrimPair][primSegPair].first == "*"){
            count++
        }
        // No caso de estar cima direita

        if(matrixTerrain[segPrimPair][centerX].first == "*"){
            count++
        }
        // No caso de estar meio direita

        if (matrixTerrain[segPrimPair][segSegPair].first == "*"){
            count++
        }
        // No caso de estar baixo direita

    }
    return count
}

fun fillNumberOfMines(matrixTerrain: Array<Array<Pair<String, Boolean>>>){
    // vai colocar os números da função anterior no array

    for(linha in 0..matrixTerrain.size - 1){

        for(coluna in 0..matrixTerrain[0].size - 1){

            val quadrado = countNumberOfMinesCloseToCurrentCell(matrixTerrain, linha, coluna)
            val stringPair = matrixTerrain[linha][coluna].first

            if(stringPair != "*" && stringPair != "P"  && stringPair != "f"){

                if(quadrado >= 1){      // se for 0 nao coloca nada
                    matrixTerrain[linha][coluna] = Pair("$quadrado", false)
                }

            }

            if(quadrado >= 1){      // excessão que não funcionava
                matrixTerrain[matrixTerrain.size - 1][matrixTerrain[0].size - 2] = Pair("$quadrado", false)
            }

        }
    }
}

fun revealMatrix(matrixTerrain: Array<Array<Pair<String, Boolean>>>, coordY: Int, coordX: Int, endGame: Boolean = false){
    // Altera o que está a volta da letra P (num raio de 1), nunca mostrando as minas. Se endGame = true, revela as minas todas

    val quadrado = getSquareAroundPoint(coordY, coordX, matrixTerrain.size - 1, matrixTerrain[0].size - 1)
    for(linha in (quadrado.first).first..(quadrado.second).first){

        for(coluna in (quadrado.first).second..(quadrado.second).second){

            if(endGame){ // mostra tudo
                matrixTerrain[linha][coluna] = Pair(matrixTerrain[linha][coluna].first, true)
            } else{ // mostra tudo menos minas, a volta do P
                if(matrixTerrain[linha][coluna] != Pair("*", false)){
                    matrixTerrain[linha][coluna] = Pair(matrixTerrain[linha][coluna].first, true)
                }
            }

        }
    }
}

fun isEmptyAround(matrixTerrain: Array<Array<Pair<String, Boolean>>>, centerY: Int, centerX: Int, yl: Int, xl: Int, yr: Int, xr: Int): Boolean{
    // vai verificar se não tem nenhuma mina a volta da mina colocada aleatóriamente, e tem que ser uma casa vazia,
    // não podendo conter nenhuma casa, mesmo a da partida e a final

    if(matrixTerrain[centerY][xl].first == "*" || matrixTerrain[centerY][xl].first == "P" || matrixTerrain[centerY][xl].first == "f"){
        return false
        // No caso de estar cima meio
    } else if(matrixTerrain[yl][centerX].first == "*" || matrixTerrain[yl][centerX].first == "P" || matrixTerrain[yl][centerX].first == "f"){
        return false
        // No caso de estar meio esquerda
    } else if(matrixTerrain[yr][centerX].first == "*" || matrixTerrain[yr][centerX].first == "P" || matrixTerrain[yr][centerX].first == "f"){
        return false
        // No caso de estar meio direita
    }else return !(matrixTerrain[centerY][xr].first == "*" || matrixTerrain[centerY][xr].first == "P" || matrixTerrain[centerY][xr].first == "f")
    // No caso de estar baixo direita
}

fun isMovementPValid(currentCoord : Pair<Int, Int>, targetCoord : Pair<Int, Int>): Boolean{
    // verifica se o movimento do jogador é feito paenas uma casa para o lado seja na diagonal, horizontal ou vertival, não podendo exceder essa casa

    val primeiroTarget = targetCoord.first
    val segundoTarget = targetCoord.second

    val primeiraCasa = currentCoord.first
    val segundaCasa = currentCoord.second

    val primCasaAMais = primeiraCasa + 1
    val segCasaAMais = segundaCasa + 1
    val primCasaAMenos = primeiraCasa - 1
    val segCasaAMenos = segundaCasa - 1

    return  if(primeiroTarget == primCasaAMais && segundoTarget == segundaCasa){
        true
        // Anda para meio direita
    } else primeiroTarget == primCasaAMenos && segundoTarget == segundaCasa
    // Anda para meio esquerda
}

fun isCoordinateInsideTerrain(coord: Pair<Int, Int>, numColumns: Int, numLines: Int): Boolean{
    //verifica se a coordenada está dentro do terreno

    return (coord.first < numLines && coord.second < numColumns && coord.first >= 0 && coord.second >= 0)
}

fun getCoordinates (readText:String?): Pair<Int, Int>? {
// mete as coordenadas da string para um pair

    if(readText != null){

        if(readText.length == 2){

            val numeros = "123456789"
            val letras = "ABCDEFGHI"
            val letrasMin = "abcdefghi"
            var countNum = 0
            var countLet = 0

            while(countNum < numeros.length && readText.first() != numeros[countNum]){
                countNum++
            }

            while(countLet < letras.length && readText.last() != letras[countLet] && readText.last() != letrasMin[countLet]){
                countLet++
            }

            if(countLet >= letras.length && countNum >= numeros.length){
                return null
            }

            return Pair(countNum , countLet)

        }else{
            return null
        }

    }else {
        return null
    }
}

fun getSquareAroundPoint(linha: Int, coluna: Int, numLines: Int, numColumns: Int): Pair<Pair<Int, Int>, Pair<Int, Int>>{
    //cria o quadrado a volta da mina gerada atoa, e verifica quando está nos cantos a mina, quando esta encostado em cada um dos extremos

    var par = Pair(Pair(linha - 1,coluna - 1), Pair(linha + 1,coluna + 1))

    if(linha == numLines && coluna == 0){
        par = Pair(Pair(linha - 1,coluna), Pair(linha,coluna + 1))
        // Baixo do inicio

    } else if(linha == 0 && coluna == numColumns) {
        par = Pair(Pair(linha, coluna - 1), Pair(linha + 1, coluna))
        // Cima da chegada

    }  else if (linha == 0 && coluna == 0){
        par = Pair(Pair(linha,coluna), Pair(linha + 1,coluna + 1))
        // Quando está na partida

    } else if(linha == numLines && coluna == numColumns ){
        par = Pair(Pair(linha - 1,coluna - 1), Pair(linha,coluna))
        // Quando está na chegada

    } else if(linha == 0){
        par = Pair(Pair(linha,coluna - 1), Pair(linha + 1,coluna + 1))
        // Em cima
    } else if(coluna == 0){
        par = Pair(Pair(linha - 1,coluna), Pair(linha + 1,coluna + 1))
        // Esquerda
    } else if(coluna == numColumns){
        par = Pair(Pair(linha - 1,coluna - 1), Pair(linha + 1,coluna))
        // Direita
    } else if(linha == numLines) {
        par = Pair(Pair(linha - 1, coluna - 1), Pair(linha, coluna + 1))
        // Em baixo
    }
    return par
}

fun erro() : String{
    // devolve erro
    return "Invalid response.\n"
}

fun main() {
    do {
        println(makeMenu())
        val menu = readLine()!!.toIntOrNull()
        if (menu == 0) { }else if(menu != 0 && menu != 1) { println(erro() )}
        if (menu == 1) {

            println("Enter player name?")
            var nome = readLine()!!.toString()
            while (!isNameValid(nome, 3)) {
                println(erro())
                println("Enter player name?")
                nome = readLine()!!.toString()
            }

            println("Show legend (y/n)?")
            var legenda = readLine()!!
            while (legenda != "y" && legenda != "n" && legenda != "Y" && legenda != "N") {
                println(erro())
                println("Show legend (y/n)?")
                legenda = readLine()!!
            }

            println("How many lines?")
            var linhas = readLine()!!.toIntOrNull()
            while (linhas == null || linhas < 4 || linhas >= 9) {
                println(erro())
                println("How many lines?")
                linhas = readLine()!!.toIntOrNull()
            }

            println("How many columns?")
            var colunas = readLine()!!.toIntOrNull()
            while (colunas == null || colunas < 4 || colunas >= 9) {
                println(erro())
                println("How many columns?")
                colunas = readLine()!!.toIntOrNull()
            }

            println("How many mines (press enter for default value)?")
            var minas = readLine()!!.toIntOrNull() ?: calculateNumMinesForGameConfiguration(linhas, colunas)
            while (minas == null || !isValidGameMinesConfiguration(linhas, colunas, minas)) {
                println(erro())
                println("How many mines (press enter for default value)?")
                minas = readLine()!!.toIntOrNull() ?: calculateNumMinesForGameConfiguration(linhas, colunas)
            }

            val matrix = createMatrixTerrain(linhas, colunas, minas)
            var showLegend = true
            if(legenda == "n" || legenda == "N") { showLegend = false }

            fillNumberOfMines(matrix)

            do{
                var terreno = makeTerrain(matrix, showLegend)
                println(terreno)
                println("Choose the Target cell (e.g 2D)")
                var coord = readLine()
                var coords = getCoordinates(coord)

                if(coord != null && coords != null){
                    while(coord == null || coords == null || ((!isCoordinateInsideTerrain(coords, linhas, colunas)
                                && !isMovementPValid(coordsAnteriores, coords)))){
                        println(erro())
                        terreno = makeTerrain(matrix, showLegend)
                        println(terreno)
                        println("Choose the Target cell (e.g 2D)")
                        coord = readLine()
                        coords = getCoordinates(coord)
                    }

                    if(coord == "abracadabra"){ terreno == makeTerrain(matrix, showLegend, true, true) }

                    if(matrix[coords.second][coords.first].first == "*"){ println("You lost the game!"); return
                    }else if (matrix[coords.second][coords.first].first == "f"){ println("You win the game!"); return}

                }
            }while (coord != "exit")
        }
    } while (menu != 1 && menu != 0)
}