import java.io.*;

public class RegCubeta {
	
	
	private byte estado;
	private byte[] codigo = new byte[20];
	private int ligaReg;
	
	public RegCubeta() {}
	
	public int length() { return (((Integer.SIZE / 8))+codigo.length + 1); }
	
	public void setEstado(byte est) {
		estado = est;
	}
	
	public void setLiga(int liga) {
		this.ligaReg = liga;
	}
	
	public byte getEstado() {
		return this.estado;
	}
	
	public int getLiga() {
		return this.ligaReg;
	}
	
	public String getCodigo() { return new String( codigo ); }
	
	public void setCodigo( String cla ) {
		codigo = new byte[ codigo.length ];
        
        if( cla.length() > codigo.length )
			System.out.println( "ATENCION: Nombre con más de 20 caracteres" );
        
        for( int i = 0; i < codigo.length && i < cla.getBytes().length; i++ )
			 codigo[i] = cla.getBytes()[i];
	}
	
	public void read( RandomAccessFile raf ) throws IOException {

		this.estado = (byte)raf.read();
		raf.read( codigo );
		ligaReg = raf.readInt();
	}

	public void write( RandomAccessFile raf ) throws IOException {
		raf.write( estado );
		raf.write( codigo );
		raf.writeInt(ligaReg);
	}
}
