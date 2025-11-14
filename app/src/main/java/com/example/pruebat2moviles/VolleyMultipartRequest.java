package com.example.pruebat2moviles;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<String> {

    private final Response.Listener<String> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mParams;
    private final String fileField;
    private final String fileName;
    private final String mimeType;
    private final byte[] fileData;

    private final String boundary = "----AndroidFormBoundary" + System.currentTimeMillis();

    public VolleyMultipartRequest(int method, String url,
                                  Map<String, String> params,
                                  String fileField, String fileName, String mimeType, byte[] fileData,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mParams = params != null ? params : new HashMap<>();
        this.fileField = fileField;
        this.fileName = fileName;
        this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
        this.fileData = fileData;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Campos de texto
            for (Map.Entry<String, String> e : mParams.entrySet()) {
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n\r\n").getBytes());
                bos.write((e.getValue() + "\r\n").getBytes());
            }
            // Archivo
            if (fileData != null && fileField != null) {
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
                bos.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes());
                bos.write(fileData);
                bos.write("\r\n".getBytes());
            }
            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException ex) {
            throw new AuthFailureError("Error construyendo multipart: " + ex.getMessage());
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    // Utilidad para leer bytes de un InputStream
    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) buffer.write(data, 0, nRead);
        return buffer.toByteArray();
    }
}