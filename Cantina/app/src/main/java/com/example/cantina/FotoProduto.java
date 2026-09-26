package com.example.cantina;

import android.content.ContentResolver;
import android.graphics.*;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;
import java.io.*;

public final class FotoProduto {
    private FotoProduto() {}
    public static String ler(ContentResolver resolver, Uri uri) throws IOException {
        BitmapFactory.Options op = new BitmapFactory.Options(); op.inJustDecodeBounds=true;
        try(InputStream in=resolver.openInputStream(uri)) { BitmapFactory.decodeStream(in,null,op); }
        if(op.outWidth<=0 || op.outHeight<=0) throw new IOException("Imagem inválida. Escolha uma foto JPEG ou PNG.");
        op.inSampleSize=1;
        while(Math.max(op.outWidth,op.outHeight)/op.inSampleSize>640) op.inSampleSize*=2;
        op.inJustDecodeBounds=false;
        Bitmap b;
        try(InputStream in=resolver.openInputStream(uri)) { b=BitmapFactory.decodeStream(in,null,op); }
        if(b==null) throw new IOException("Não foi possível abrir a imagem.");
        float fator=Math.min(1f,320f/Math.max(b.getWidth(),b.getHeight()));
        Bitmap mini=Bitmap.createScaledBitmap(b,Math.max(1,Math.round(b.getWidth()*fator)),Math.max(1,Math.round(b.getHeight()*fator)),true);
        ByteArrayOutputStream out=new ByteArrayOutputStream(); mini.compress(Bitmap.CompressFormat.JPEG,80,out);
        if(mini!=b) mini.recycle(); b.recycle();
        return Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP);
    }
    public static void mostrar(ImageView imagem,String foto) {
        imagem.setImageResource(android.R.drawable.ic_menu_gallery);
        if(foto==null || foto.isEmpty()) return;
        try { byte[] bytes=Base64.decode(foto,Base64.DEFAULT); Bitmap b=BitmapFactory.decodeByteArray(bytes,0,bytes.length); if(b!=null) imagem.setImageBitmap(b); }
        catch(IllegalArgumentException ignored) { imagem.setImageResource(android.R.drawable.ic_menu_gallery); }
    }
}
