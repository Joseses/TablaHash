import java.io.*;
import java.lang.*;

public class Registro {

    public final byte NOT_DELETED = 0;
    public final byte DELETED = 1;
    
    private byte borrado = NOT_DELETED;
	private byte[] sucursal = new byte[20];
	private int numero = 0;
	private byte[] nombre = new byte[20];
	private double saldo = 0;
    
    /*-----------------------------------------------------------------
    / constructores
    /-----------------------------------------------------------------*/
    
	public Registro() {}
    
	public Registro( String nomSucursal, int numCuenta,
					String nomCliente, double deposito ){
		setSucursal( nomSucursal );
		numero = numCuenta;
		setNombre( nomCliente );
		saldo = deposito;
	}
    
    /*-----------------------------------------------------------------
    / métodos getters / setters
    /-----------------------------------------------------------------*/
    
	public String getSucursal() { return new String( sucursal ); }
    
    public void setSucursal( String suc ) {
        
        sucursal = new byte[ sucursal.length ];
        
        if( suc.length() > sucursal.length )
			System.out.println( "ATENCION: Sucursal con más de 20 caracteres" );
        
		for( int i = 0; i < sucursal.length && i < suc.getBytes().length; i++ )
			 sucursal[i] = suc.getBytes()[i];
    }
    
    public int getNumero() { return numero; }
    
    public String getNombre() { return new String( nombre ); }
    
    public void setNombre( String nom ) {
        
        nombre = new byte[ nombre.length ];
        
        if( nom.length() > nombre.length )
			System.out.println( "ATENCION: Nombre con más de 20 caracteres" );
        
        for( int i = 0; i < nombre.length && i < nom.getBytes().length; i++ )
			 nombre[i] = nom.getBytes()[i];
    }
    
    public double getSaldo() { return saldo; }
    
    /*-----------------------------------------------------------------
    / longitud en bytes de un registro
    /-----------------------------------------------------------------*/
    
	public int length() {
        
		return sucursal.length + Integer.SIZE / 8 +
               nombre.length + Double.SIZE / 8 + 1;
	}
    
    /*-----------------------------------------------------------------
    / m�todos para escribir y leer un registro
    /-----------------------------------------------------------------*/
    
    public void read( RandomAccessFile raf ) throws IOException {
        
		borrado = (byte) raf.read();
		raf.read( sucursal );
		numero = raf.readInt();
		raf.read( nombre );
		saldo = raf.readDouble();
	}
    
	public void write( RandomAccessFile raf ) throws IOException {
        
        raf.write( borrado );
		raf.write( sucursal );
		raf.writeInt( numero );
		raf.write( nombre );
		raf.writeDouble( saldo );
	}
}
