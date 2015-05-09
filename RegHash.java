
/**
 *
 * @author Joseses
 */

import java.io.IOException;
import java.io.RandomAccessFile;

public class RegHash
{
    
    private byte[] liga;
    
    
    /*-----------------------------------------------------------------
    / constructor
    /-----------------------------------------------------------------*/
    
    public RegHash(){}
        
    public RegHash( int longitud ) { liga = new byte[ longitud ]; }
    
    /*-----------------------------------------------------------------
    / m�todos getters/setters
    /-----------------------------------------------------------------*/
    
    public String getLiga() { return new String( liga ); }
    
    public void setLiga( String valor ) {
        byte[] v = valor.getBytes();
        
        for( int i = 0; i < liga.length && i < v.length; i++ )
            liga[i] = v[i];
	
    }
    
    
    /*-----------------------------------------------------------------
    / longitud en bytes y comparaci�n del valor de la clave
    /-----------------------------------------------------------------*/
    
    public int length() { return liga.length; }
    
    public int compararCon( String valor ) {
        
        byte[] k = valor.getBytes();
	byte[] v = new byte[ liga.length ];
        
	for( int i = 0; i < liga.length && i < k.length; i++ )
             v[i] = k[i];
        
	return getLiga().compareTo(new String(v) );
	
    }
    
    /*-----------------------------------------------------------------
    / m�todos para escribir y leer una entrada en el �ndice
    /-----------------------------------------------------------------*/
    
    public void read( RandomAccessFile raf ) throws IOException {
        
		raf.read( liga );
		
               // liga = raf.readInt();
    }
    
    public void write( RandomAccessFile raf ) throws IOException {
        
        raf.write( liga );
	
        //raf.writeInt( liga );
	
    }
    
}
