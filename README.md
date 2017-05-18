"# ArduinoBand" 
ArduinoBand es una coneccion entre un Arduino Pro micro y Una App desarrollada en android Studio
Tiene una opcion de envio de orden que para este caso se conecta atravez de bluetooth 
con el arduino a travez de su modulo Bluetooth HC-05 y se puede enviar atravez del boton "Sonar"
Envia el numero "1" este es recibido y enciende un led y al mismo tiempo a travez del mismo pin
enciende un vibrador.
La segunda opcion es el modo escaner, el cual es un escaner de señal RSSI solo al dispositivo con el 
que se hizo el PAIRED al inicial la app, y por medio de un SeekBar se configura una alerta a travez
del Notification Manager dependiendo del valor asignado en el SeekBar. 
El Archivo ArduinoBT.ino contiene la programacion en la placa arduino.

El dispositivo Arduino debe estar como esclavo y luego debe emparejarse al movil donde se instale la APP
