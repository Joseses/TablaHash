import java.io.File;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Disco {
	
	public static void main(String[] Joseses){
		Scanner sc = new Scanner(System.in);
		int option;
		
		try { //Previene cuando el usuario escribe cualquier cosa menos un número.
			do{
				showMenu();
				option = sc.nextInt();
				setMenu(option);
			}while(option != 5);
		} catch (Exception e) {
			System.out.println("Caracter inválido");
		}
	}
    
	private static void showMenu(){
		System.out.print("Con el numero correspondiente, elija una opcion del menu: "+ "\n" +
				"1) Crear un Registro"+ "\n"+
				"2) Eliminar un Registro"+"\n"+
				"3) Buscar un Registro(Busqueda Lineal)"+"\n"+
				"4) Imprimir todos los Registros"+"\n"+
				"5) Salir del programa"+"\n"+
				 "6) Crear 100mil registros Aleatoriamente(1000 registros por 100 entradas)"+"\n"+
				"====== Opción: ");
	}
    
	public static void setMenu(int option){
		try {
			// metadatos del archivo de datos y del archivo índice
			File datos = new File( "Archivo.Datos" );
			File hash = new File( "Indice.Hash" );
			File cubetas = new File("Indice.Cubetas");

			// handlers para manipular el contenido de los archivos
			RandomAccessFile archivoRaF = new RandomAccessFile( datos, "rw" );
			RandomAccessFile indiceRaF = new RandomAccessFile( hash, "rw" );
			RandomAccessFile cubetasRaF = new RandomAccessFile( cubetas, "rw" );
			 // archivo indexado usando una clave de búsqueda de 20 bytes
			Archivo archivo = new Archivo( archivoRaF, indiceRaF, cubetasRaF);
            
			Registro registro;
			
			Scanner sc = new Scanner(System.in);
			if(option == 1){
				System.out.println("--------------------------------------------------------------");
				System.out.print("Introduzca el nombre de la sucursal: ");
				String suc = sc.nextLine();
				System.out.print("Introduzca el número de cuenta: ");
				int num = sc.nextInt();
				sc.nextLine();
				System.out.print("Introduzca el nombre del titular: ");
				String nom = sc.nextLine();
				System.out.print("Introduzca la cantidad de la cuenta: ");
				double sal = sc.nextDouble();
				Registro nuevo = new Registro(suc, num, nom, sal);
				archivo.insertar(nuevo);
				System.out.println("-------------------------Registro Agregado-------------------------");
			}else if(option == 2) {
				System.out.println("--------------------------------------------------------------");
				//archivo.mostrar();
				System.out.println("Introduzca el nombre de la Sucursal del registro a eliminar");
				System.out.print("(Se eliminara el primer registro encontrado de la sucursal): ");
				int suc = sc.nextInt();
				System.out.println("--------------------------------------------------------------");
				System.out.println("--------------------------------------------------------------");
			}else if(option == 3){
				System.out.println("--------------------------------------------------------------");
				System.out.print("Introduzca el numero de cuenta: ");
				int num = sc.nextInt();
				System.out.println("--------------------------------------------------------------");
                                archivo.busquedaLineal(num);
				System.out.println("--------------------------------------------------------------");
			}else if(option == 4){
				System.out.println("--------------------------------------------------------------");
				archivo.mostrar();
				System.out.println("--------------------------------------------------------------");
                                archivo.imprimirTodo();
				System.out.println("--------------------------------------------------------------");
			}else if(option == 5){
				System.out.println("--------------------------------------------------------------");
				System.out.println("Salir, Adios");
				archivo.cerrar();
			}else if(option == 6){
				System.out.println("--------------------------------------------------------------");
				System.out.println("Creando registros");
				for( int num = 1, i = 1; i <= 100; i++ ) { //Numero de Surcursales
					for( int j = 1; j <= 1000; j++ ) { //Numero de clientes
						for( int k = 1; k <= 1; k++, num++ ) {
							String suc = "Sucursal " + String.format( "%3d", i );
							String nom = "Cliente " + j;
							double salMin = 100.0, salMax = 30000.6;
							double sal = Math.random() * (salMax - salMin) + salMin;
							archivo.insertar( new Registro( suc, num, nom, sal ) );
                    	}
                	}
            	}
				System.out.println("Registros creados");
			}else{
				System.out.println("--------------------------------------------------------------");
				System.out.println("Error, Opción invalida");
				System.out.println("--------------------------------------------------------------");
			}
		} catch( Exception e ) {
            
			System.out.println( "IOException:" );
			e.printStackTrace();
                        
		}
	}
}
