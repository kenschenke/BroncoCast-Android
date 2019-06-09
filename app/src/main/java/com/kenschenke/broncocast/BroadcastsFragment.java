package com.kenschenke.broncocast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.GET;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BroadcastsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BroadcastsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BroadcastsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public BroadcastsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BroadcastsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BroadcastsFragment newInstance(String param1, String param2) {
        BroadcastsFragment fragment = new BroadcastsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_broadcasts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchBroadcasts();
    }

    public class broadcastComp implements Comparator<Map<String,String>> {
        public int compare(Map<String,String> b1, Map<String,String> b2) {
            int t1 = Integer.parseInt(b1.get("timestamp"));
            int t2 = Integer.parseInt(b2.get("timestamp"));

            if (t1 < t2) {
                return 1;
            } else if (t1 > t2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private void fetchBroadcasts() {
        UrlMaker urlMaker = UrlMaker.getInstance(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, urlMaker.getUrl(UrlMaker.URL_BROADCASTS), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("Success")) {
                        Toast.makeText(getActivity(), response.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    ListView listView = getView().findViewById(R.id.listView);

                    final List<Map<String,String>> broadcastData = new ArrayList<>();

                    JSONArray broadcasts = response.getJSONArray("Broadcasts");
                    for (int i = 0; i < broadcasts.length(); i++) {
                        JSONObject broadcast = broadcasts.getJSONObject(i);

                        Map<String,String> broadcastInfo = new HashMap<>();
                        broadcastInfo.put("sentBy", broadcast.getString("UsrName"));
                        broadcastInfo.put("delivered", broadcast.getString("Delivered"));
                        broadcastInfo.put("shortMsg", broadcast.getString("ShortMsg"));
                        broadcastInfo.put("longMsg", broadcast.getString("LongMsg"));
                        broadcastInfo.put("timestamp", broadcast.getString("Timestamp"));
                        broadcastData.add(broadcastInfo);
                    }

                    Collections.sort(broadcastData, new broadcastComp());

                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), broadcastData,
                            android.R.layout.simple_list_item_2,
                            new String[] {"delivered", "shortMsg"},
                            new int[] {android.R.id.text1, android.R.id.text2});
                    listView.setAdapter(simpleAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(view.getContext(), BroadcastDetailActivity.class);

                            intent.putExtra("SentBy", broadcastData.get(position).get("sentBy"));
                            intent.putExtra("Delivered", broadcastData.get(position).get("delivered"));
                            intent.putExtra("ShortMsg", broadcastData.get(position).get("shortMsg"));
                            intent.putExtra("LongMsg", broadcastData.get(position).get("longMsg"));

                            startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error", error.toString());
                Toast.makeText(getActivity(), "Unable to contact server", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getActivity().getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getActivity().getApplication();
        app.getRequestQueue().add(jsonObjectRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBroadcasts();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
