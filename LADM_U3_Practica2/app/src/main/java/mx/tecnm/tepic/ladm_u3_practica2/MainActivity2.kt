package mx.tecnm.tepic.ladm_u3_practica2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extras = intent.extras

        editText1A2.setText(extras!!.getString("lugar"))
        editText2A2.setText(extras!!.getString("hora"))
        editText3A2.setText(extras!!.getString("fecha"))
        editText4A2.setText(extras!!.getString("descripcion"))

        id = extras.getString("id").toString()

        actualizar.setOnClickListener {
            var actualizacion = evento(editText1A2.text.toString(), editText2A2.text.toString(), editText3A2.text.toString(), editText4A2.text.toString())
            actualizacion.id = id.toInt()
            actualizacion.asignarPuntero(this)

            Toast.makeText(this,actualizacion.id.toString()+actualizacion.lugar+actualizacion.hora+actualizacion.fecha+actualizacion.descripcion, Toast.LENGTH_LONG)
                .show()

            if(actualizacion.actualizar()) {
                Toast.makeText(this,"SE ACTUALIZÓ CORRECTAMENTE", Toast.LENGTH_LONG)
                    .show()
                var ventana =
                finish()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("ERROR")
                    .setMessage("NO SE PUDO ACTUALIZAR")
                    .setPositiveButton("OK"){d,i->}
                    .show()
            }
            finish()
        }
        regresar.setOnClickListener {
            finish()
        }
    }
}