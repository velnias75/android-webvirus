/*
 * Copyright 2019-2020 by Heiko Schäfer <heiko@rangun.de>
 *
 *  This file is part of android-webvirus.
 *
 *  android-webvirus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  android-webvirus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with android-webvirus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Last modified 17.02.20 06:21 by heiko
 */

/* Based on: https://gist.github.com/premnirmal/8526542 */

package de.rangun.webvirus.net;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class GZipJsonObjectRequest<T> extends JsonObjectRequest {

    private final T userParam;
    private final OutputStream gzipOut;

    private final static class saveGZIP extends AsyncTask<Void, Void, Void> {

        private final OutputStream gzipOut;
        private final NetworkResponse response;

        saveGZIP(OutputStream gzipOut, NetworkResponse response) {
            this.gzipOut  = gzipOut;
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                gzipOut.write(response.data, 0, response.data.length);
            } catch(IOException ex) {
                Log.w("GZipJsonObjectRequest", ex);
            } finally {
                try {
                    gzipOut.close();
                } catch(IOException ex) {
                    Log.w("GZipJsonObjectRequest", ex);
                }
            }

            return null;
        }
    }

    public GZipJsonObjectRequest(int method, String url, @Nullable JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 @Nullable Response.ErrorListener errorListener,
                                 T userParam, OutputStream gzipOut) {
        super(method, url, jsonRequest, listener, errorListener);
        this.userParam = userParam;
        this.gzipOut = gzipOut;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<>(super.getHeaders());
        params.put("Accept-Encoding", "gzip,deflate");
        return params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        if(!"gzip".equals(response.headers.get("Content-Encoding"))) {

            final Response<JSONObject> myResponse = super.parseNetworkResponse(response);

            if(myResponse.isSuccess()) {
                return Response.success(customParse(myResponse.result, userParam),
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return myResponse;
            }
        }

        final StringBuilder output = new StringBuilder();

        try {

            final GZIPInputStream gStream =
                    new GZIPInputStream(new ByteArrayInputStream(response.data),
                            response.data.length);
            final InputStreamReader reader = new InputStreamReader(gStream, StandardCharsets.UTF_8);
            final BufferedReader in = new BufferedReader(reader, 65536);

            if(gzipOut != null) (new saveGZIP(gzipOut, response)).execute();

            String read;

            while((read = in.readLine()) != null) { output.append(read); }

            reader.close();
            in.close();
            gStream.close();

        } catch(IOException ie) {
            return Response.error(new ParseError(ie));
        }

        try {

            return Response.
                    success(customParse(new JSONObject(new String(output.toString().
                                    getBytes(StandardCharsets.UTF_8),
                                    HttpHeaderParser.parseCharset(response.headers,
                                            PROTOCOL_CHARSET))), userParam),
                            HttpHeaderParser.parseCacheHeaders(response));

        } catch(UnsupportedEncodingException | JSONException ue) {
            return Response.error(new ParseError(ue));
        }
    }

    @SuppressWarnings({"unused", "EmptyMethod"})
    protected abstract JSONObject customParse(JSONObject jsonObject, T userParam);
}
