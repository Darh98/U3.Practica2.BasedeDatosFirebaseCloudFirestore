package mx.tecnm.tepic.ladm_u3_practica2

import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter

import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var listaID = ArrayList<String>()
    var base = BaseDatos(this, "agenda", null, 1)

    //FIREBASE
    var baseRemota = FirebaseFirestore.getInstance()
    var datosRemotos = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertar.setOnClickListener {
            var evento = evento(
                editText1.text.toString(),
                editText2.text.toString(),
                editText3.text.toString(),
                editText4.text.toString()
            )

            evento.asignarPuntero(this)

            var res = evento.insertar()

            if(res == true) {
                mensaje("LA DATA SE CAPTURÓ CORRECTAMENTE")
                editText1.setText("")
                editText2.setText("")
                editText3.setText("")
                editText4.setText("")
                cargaInformacion()
            } else {
                mensaje("HUBO UN ERROR AL CAPTURAR LA DATA")
            }
        }
        cargaInformacion()

        sincronizar.setOnClickListener {
            sincronizar()
        }
    }

    private fun cargaInformacion(){
        try {
            var c = evento("","","","")
            c.asignarPuntero(this)
            var datos = c.recuperarDatos()

            var tamaño = datos.size-1
            var v = Array<String>(datos.size,{""})

            listaID = ArrayList<String>()
            (0..tamaño).forEach {
                var evento = datos[it]
                var item = evento.lugar+"\n"+evento.hora+"\n"+evento.fecha+"\n"+evento.descripcion
                v[it] = item
                listaID.add(evento.id.toString())
            }

            lista.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,v)

            lista.setOnItemClickListener{ adapterView, view, i, l ->
                mostrarAlertEliminarActualizar(i)
            }
        }catch (e: Exception){
            mensaje(e.message.toString())
        }
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this).setTitle("ATENCION").setMessage(s)
            .setPositiveButton("OK"){
                    d,i-> d.dismiss()
            }
            .show()
    }

    private fun sincronizar() {
        datosRemotos.clear()
        baseRemota.collection("agenda")
            .addSnapshotListener {querySnapshot, firebaseFirestoreException ->

                if(firebaseFirestoreException !=null) {
                    mensaje("NO SE PUDO REALIZAR CONEXION CON LA NUBE")
                    return@addSnapshotListener
                }

                var cadena= ""

                for(registro in querySnapshot!!)
                {
                    cadena=registro.id
                    datosRemotos.add(cadena)
                }

                try {
                    var c = base.readableDatabase
                    var res = c.query("AGENDA", arrayOf("*"),null,null,null,null,null)

                    if(res.moveToFirst())
                    {
                        do {
                            if(datosRemotos.contains(res.getString(0))) {
                                baseRemota.collection("agenda")
                                    .document(res!!.getString(0))
                                    .update("LUGAR",res!!.getString(1),"HORA",res!!.getString(2), "FECHA",res!!.getString(3), "DESCRIPCION", res!!.getString(4))
                                    .addOnFailureListener {
                                        AlertDialog.Builder(this)
                                            .setTitle("ERROR")
                                            .setMessage("NO SE PUDO ESPEJEAR LA ACTUALIZACION\n${it.message!!}")
                                            .setPositiveButton("OK"){d,i->}
                                            .show()
                                    }
                            } else {
                                var datosInsertar = hashMapOf(
                                    "LUGAR" to res!!.getString(1),
                                    "HORA" to res!!.getString(2),
                                    "FECHA" to res!!.getString(3),
                                    "DESCRIPCION" to res!!.getString(4)
                                )

                                baseRemota.collection("agenda").document("${res!!.getString(0)}")
                                    .set(datosInsertar as Any)
                                    .addOnFailureListener{
                                        mensaje("NO SE PUDO ESPEJEAR LA INSERCION\n${it.message!!}")
                                    }
                            }
                        }while(res.moveToNext())
                    } else {
                        datosRemotos.add("NO SE HAN ENCONTRADO CAMBIOS QUE ESPEJEAR")
                    }
                    c.close()
                } catch (e: SQLiteException) {
                    mensaje("ALGO SALIÓ MAL: " + e.message!!)
                }
                deleteRemoto()
            }
            mensaje("LA SINCRONIZACION SE LLEVÓ A CABO SATISFACTORIAMENTE")
    }

    private fun deleteRemoto() {
        var eliminarRemoto= datosRemotos.subtract(listaID)
        if(eliminarRemoto.isEmpty()) {
        } else {
            eliminarRemoto.forEach(){
                baseRemota.collection("agenda")
                    .document(it)
                    .delete()
                    .addOnFailureListener{
                        mensaje("NO SE PUDO ESPEJEAR LA ELIMINACION\n${it.message!!}")
                    }
            }
        }
    }

    private fun mostrarAlertEliminarActualizar(posicion: Int) {
        var idLista = listaID.get(posicion)

        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage("¿Qué deseas hacer con este registro?")
            .setPositiveButton("ELIMINAR") {d,i-> eliminar(idLista)}
            .setNeutralButton("CANCELAR") {d,i->}
            .setNegativeButton("ACTUALIZAR") {d,i-> llamarVentanaActualizar(idLista)}
            .show()
    }

    private fun eliminar(id:String) {
        var c = evento("","","","")
        c.asignarPuntero(this)

        if(c.eliminar(id)) {
            mensaje("SE HA ELIMINADO EL REGISTRO CORRECTAMENTE")
            cargaInformacion()
        } else {
            mensaje("OCURRIÓ UN ERROR EN LA ELIMINACION DEL REGISTRO")
        }
    }

    private fun llamarVentanaActualizar(idLista: String) {
        var ventana = Intent(this,MainActivity2::class.java)
        var c = evento("","","","")
        c.asignarPuntero(this)

        var l = c.consultaID(idLista).lugar
        var h = c.consultaID(idLista).hora
        var f = c.consultaID(idLista).fecha
        var d = c.consultaID(idLista).descripcion

        ventana.putExtra("id",idLista)
        ventana.putExtra("lugar",l)
        ventana.putExtra("hora",h)
        ventana.putExtra("fecha",f)
        ventana.putExtra("descripcion",d)

        startActivityForResult(ventana,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cargaInformacion()
    }
}