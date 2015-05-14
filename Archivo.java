import java.io.*;

public class Archivo {

	private RandomAccessFile raf = null;
	public TabladeHash tablahash = null;

	public Archivo(RandomAccessFile archivo, RandomAccessFile indice,
					RandomAccessFile cubetas) {
		raf = archivo;
		tablahash = new TabladeHash(indice, cubetas);
	}

	public void cerrar() throws IOException {
		raf.close();
		tablahash.cerrar();
	}
	
	public void insertar(Registro registro) throws IOException{
		insertarEn(((int)raf.length()/registro.length()), registro);
		System.out.println("Se inserta el numero " + registro.getNumero());
		tablahash.insertarEntrada(registro, (int)raf.length()-registro.length());
	}
	
	
	public void imprimirTodo() throws IOException {
		
		Registro registro = new Registro();
		int length = (int) (raf.length() / registro.length());
		System.out.println( "Número de registros: " + length );
		raf.seek( 0 );
		for( int i = 0; i < length; i++ ) {
			
			registro.read( raf );
			System.out.println( "( " + registro.getSucursal() + ", "
                                     + registro.getNumero() + ", "
                                     + registro.getNombre() + ", "
                                     + registro.getSaldo() + ", "
                                     /*+ registro.getEliminado() + " )" */);
		}
	}
	
	private void insertarEn( int posicion, Registro registro ) throws IOException {

		int n = (int) raf.length() / registro.length();

		for( int i = n-1; i >= posicion; i -- ) {

			Registro temp = new Registro();

			raf.seek( i * temp.length() );
			temp.read( raf );

			raf.seek( (i+1) * temp.length() );
			temp.write( raf );
		}

		raf.seek( posicion * registro.length() );
		registro.write( raf );
	}
	
	public void mostrar() throws IOException{
		System.out.println("-----REGISTROS EN LA TABLA-----");
		tablahash.mostrar();
		System.out.println("-------------------------------------------");
	}
}
