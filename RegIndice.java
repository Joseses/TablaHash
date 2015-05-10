import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by josema on 14/04/15.
 */
public class RegIndice {

	private byte[] clave = new byte[20];
	private int liga;

	/*-----------------------------------------------------------------
	/ constructor
	/-----------------------------------------------------------------*/

	public RegIndice(){}

	public int length() { return (((Integer.SIZE / 8))+clave.length); }

	public void setClave( String cla ) {
		clave = new byte[ clave.length ];
        
        if( cla.length() > clave.length )
			System.out.println( "ATENCION: Nombre con más de 20 caracteres" );
        
        for( int i = 0; i < clave.length && i < cla.getBytes().length; i++ )
			 clave[i] = cla.getBytes()[i];
	}
	
	public String getClave() {
		return new String (clave);
	}

	public int getLiga() { return liga; }

	public void setLiga( int posicion ) { liga = posicion; }

	public void read( RandomAccessFile raf ) throws IOException {

		raf.read(clave);
		liga = raf.readInt();
	}

	public void write( RandomAccessFile raf ) throws IOException {

		raf.write( clave );
		raf.writeInt(liga);
	}

}
