package com.example.byyourside


interface VerificacionCampos {


    fun validarNifComercio(nif: String): Boolean {
        // Regex: Empieza con una letra (A-Z, case-insensitive), opcionalmente un guion '-', y termina con 8 dígitos.
        val regex = Regex("^[A-Z][-]?\\d{8}\$", RegexOption.IGNORE_CASE)
        return regex.matches(nif.trim())
    }


    fun validarNombreComercio(nombre: String): Boolean{
        // Regex: Permite letras unicode, números, espacios, y los caracteres '.', ''', '-'.
        val regex = Regex("^[\\p{L}0-9 .'-]+$", RegexOption.IGNORE_CASE)
        return regex.matches(nombre.trim())
    }


    fun validarNombreApellidosCliente(nombreApellidos: String): Boolean{
        // Regex: Solo permite letras unicode y espacios.
        val regex = Regex("^[\\p{L} ]+$")
        return regex.matches(nombreApellidos.trim())
    }


    fun validarNombreCalleComercio(calle: String): Boolean {
        // Si el campo está vacío, se considera válido (opcional).
        if(calle.isBlank()) {
            return true
        }
        // Regex: Solo permite letras unicode y espacios.
        val regex = Regex("^[\\p{L} ]+$")
        return regex.matches(calle.trim())
    }


    fun validarNumeroLocal(entrada: String): Boolean {
        // Regex: La cadena debe contener uno o más dígitos numéricos.
        val regex = Regex("^[0-9]+$")
        return regex.matches(entrada.trim())
    }


    fun validar_codigo_postal(entrada: String): Boolean {
        // Regex: Acepta entre 1 y 5 dígitos.
        val regex = Regex("^\\d{1,5}$")
        return regex.matches(entrada.trim())
    }


    fun validarWeb(web: String): Boolean{
        // Si el campo está vacío, se considera válido (opcional).
        if(web.isBlank()){
            return true
        }
        // Regex: Formato estándar de URL.
        val regex = Regex("^(https?:\\/\\/)?([\\w-]+\\.)+[\\w-]+(\\/[^\\s]*)?\$", RegexOption.IGNORE_CASE)
        return regex.matches(web.trim())
    }


    fun validarEmail(e_mail: String): Boolean{
        // Regex: Formato estándar de email.
        val regex = Regex( "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$", RegexOption.IGNORE_CASE)
        return regex.matches(e_mail.trim())
    }


    fun validarTelefono(telefono: String): Boolean {
        // Regex: Empieza por un dígito entre 6 y 9, seguido de 8 dígitos más.
        val regex = Regex("^[6-9][0-9]{8}\$")
        return regex.matches(telefono.trim())
    }


    fun validarContrasenha(contrasenha: String): Boolean {
        // Regex: Lookaheads para asegurar la presencia de a-z, A-Z y un dígito, con una longitud de 8 a 20 caracteres.
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,20}\$")
        return regex.matches(contrasenha.trim())
    }


    fun validarMarcaNombreProducto(nombre: String): Boolean{
        // Regex: idéntica a la de validarNombreComercio.
        val regex = Regex("^[\\p{L}0-9 .'-]+$", RegexOption.IGNORE_CASE)
        return regex.matches(nombre.trim())
    }


    fun validarIdProducto(id: String): Boolean {
        // Regex: Empieza con 1 o 2 letras, seguido opcionalmente de un guion '-', y termina con 5 dígitos.
        val regex = Regex("^[A-Za-z]{1,6}-?\\d{1,6}\$")
        return regex.matches(id.trim())
    }


    fun validarLoteProducto(lote: String): Boolean {
        // Regex: Empieza con una letra, seguido opcionalmente de un guion '-', y de 0 a 15 dígitos.
        val regex = Regex("^[A-Z][-]?\\d{0,15}\$", RegexOption.IGNORE_CASE)
        // Se comprueba tanto la longitud máxima como el formato de la regex.
        return lote.trim().length <= 16 && regex.matches(lote.trim())
    }


    fun validarPrecioProducto(precio: String): Boolean {
        // Regex: Acepta solo dígitos, o una combinación de dígitos (opcionales), un punto, y 1 o 2 dígitos decimales.
        val regex = Regex("^((\\d+)|(\\d*\\.\\d{1,2}))$")
        return regex.matches(precio.trim())
    }
}