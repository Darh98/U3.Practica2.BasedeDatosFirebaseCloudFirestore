package mx.tecnm.tepic.ladm_u3_practica2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import java.util.*

class evento(l:String, h:String, f: String, d:String) {
    var id = 0
    var lugar = l
    var hora = h
    var fecha = f
    var descripcion = d

    val nombre = "agenda"
    var puntero : Context?= null

    fun asignarPuntero(p:Context) {
        puntero = p
    }

    fun insertar():Boolean {
        try {
            var base = BaseDatos(puntero!!, nombre, null, 1)
            var insertar = base.writableDatabase
            var datos = ContentValues()

            datos.put("LUGAR", lugar)
            datos.put("HORA", hora)
            datos.put("FECHA", fecha)
            datos.put("DESCRIPCION", descripcion)

            var res = insertar.insert("AGENDA", "IDAGENDA", datos)

            if (res.toInt() == -1) {
                return false
            }
        }catch (e: SQLiteException) {
            return false
        }
        return true
    }

    fun recuperarDatos():ArrayList<evento>{
        var data = ArrayList<evento>()
        try{
            var bd = BaseDatos(puntero!!,nombre,null,1 )
            var select = bd.readableDatabase
            var columnas = arrayOf("*")

            var cursor  = select.query("AGENDA", columnas, null, null, null, null, null)
            if(cursor.moveToFirst()){
                do{
                    var temp = evento(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4))

                    temp.id = cursor.getInt(0)
                    data.add(temp)
                }while (cursor.moveToNext())
            }else{
            }
        }catch (e:SQLiteException){
        }
        return data
    }

    fun consultaID(id:String): evento{
        var registro = evento("","","","")

        try {
            var bd = BaseDatos(puntero!!, nombre, null, 1)
            var select = bd.readableDatabase
            var busca = arrayOf("*")
            var buscaID = arrayOf(id)

            var res = select.query("AGENDA", busca, "IDAGENDA =?",buscaID, null, null, null)
            if(res.moveToFirst()){
                registro.id = id.toInt()
                registro.lugar = res.getString(1)
                registro.hora = res.getString(2)
                registro.fecha = res.getString(3)
                registro.descripcion = res.getString(4)
            }
        }catch (e:SQLiteException){
            e.message.toString()
        }
        return registro
    }

    fun eliminar(id:String):Boolean{
        try{
            var base = BaseDatos(puntero!!, nombre,null,1)
            var eliminar = base.writableDatabase
            var eliminarID = arrayOf(id)

            var res = eliminar.delete("AGENDA","IDAGENDA = ?",eliminarID)
            if(res.toInt() == 0){
                return false
            }
        }catch (e:SQLiteException){
            return false
        }
        return true
    }

    fun actualizar():Boolean{
        try{
            var base = BaseDatos(puntero!!, nombre,null,1)
            var actualizar = base.writableDatabase
            var datos = ContentValues()
            var actualizarID = arrayOf(id.toString())

            datos.put("LUGAR", lugar)
            datos.put("HORA", hora)
            datos.put("FECHA", fecha)
            datos.put("DESCRIPCION", descripcion)

            var res = actualizar.update("AGENDA",datos,"IDAGENDA = ?", actualizarID)
            if(res.toInt()== 0){
                return false
            }
        }catch (e:SQLiteException){
            return false
        }
        return true
    }
}