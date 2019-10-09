/*
 * Copyright 2019 by Heiko Schäfer <heiko@rangun.de>
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
 *  Last modified 06.10.19 08:07 by heiko
 */

/* Based on: https://gist.github.com/premnirmal/8526542 */

package de.rangun.webvirus.net;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class GZipJsonArrayRequest<T> extends JsonArrayRequest {

    private final T userParam;

    public GZipJsonArrayRequest(int method, String url, @Nullable JSONArray jsonRequest,
                                Response.Listener<JSONArray> listener,
                                @Nullable Response.ErrorListener errorListener,
                                T userParam) {
        super(method, url, jsonRequest, listener, errorListener);
        this.userParam = userParam;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<>(super.getHeaders());
        params.put("Accept-Encoding", "gzip,deflate");
        return params;
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {

        if(!"gzip".equals(response.headers.get("Content-Encoding"))) {

            final Response<JSONArray> myResponse = super.parseNetworkResponse(response);

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
            final InputStreamReader reader = new InputStreamReader(gStream);
            final BufferedReader in = new BufferedReader(reader, 65536);

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
                    success(customParse(new JSONArray(new String(output.toString().getBytes(),
                                    HttpHeaderParser.parseCharset(response.headers,
                                            PROTOCOL_CHARSET))), userParam),
                            HttpHeaderParser.parseCacheHeaders(response));

        } catch(UnsupportedEncodingException | JSONException ue) {
            return Response.error(new ParseError(ue));
        }
    }

    @SuppressWarnings({"unused", "EmptyMethod"})
    protected abstract JSONArray customParse(JSONArray array, T userParam);
}
