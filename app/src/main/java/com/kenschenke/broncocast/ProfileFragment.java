package com.kenschenke.broncocast;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements TextFieldHelperCallable {

    private TextView textViewName, textViewNameHelp;
    private EditText editTextName;
    private Switch switchSingleMsg;
    private TextFieldHelper nameHelper;
    private String lastSavedName;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewName = getView().findViewById(R.id.textViewName);
        textViewNameHelp = getView().findViewById(R.id.textViewNameHelp);
        editTextName = getView().findViewById(R.id.editTextName);
        switchSingleMsg = getView().findViewById(R.id.switchSingleMsg);
        nameHelper = new TextFieldHelper(editTextName, textViewName, textViewNameHelp);
        nameHelper.callable = this;

        switchSingleMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        textViewNameHelp.setText("");

        Button btnAddEmail = getView().findViewById(R.id.btnAddEmail);
        btnAddEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getView().getContext(), EmailContactActivity.class);
                intent.putExtra("IsNew", true);
                intent.putExtra("Contact", "");
                intent.putExtra("ContactId", 0);

                startActivityForResult(intent, 1);
            }
        });

        Button btnAddPhone = getView().findViewById(R.id.btnAddPhone);
        btnAddPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getView().getContext(), PhoneContactActivity.class);
                intent.putExtra("IsNew", true);
                intent.putExtra("Contact", "");
                intent.putExtra("ContactId", 0);

                startActivityForResult(intent, 1);
            }
        });

        fetchProfile();
        fetchContacts();
    }

    @Override
    public void onTextFieldChange(EditText editText, String value) {
        String name = editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            textViewNameHelp.setText("Name cannot be empty");
            nameHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            return;
        }

        if (!name.equals(lastSavedName)) {
            saveProfile();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // User clicked Save or Delete instead of back button
                fetchContacts();
            }
        }
    }

    public class contactComp implements Comparator<Map<String,String>> {
        public int compare(Map<String,String> b1, Map<String,String> b2) {
            int t1 = Integer.parseInt(b1.get("contactId"));
            int t2 = Integer.parseInt(b2.get("contactId"));

            if (t1 < t2) {
                return 1;
            } else if (t1 > t2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private void fetchContacts() {
        UrlMaker urlMaker = UrlMaker.getInstance(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, urlMaker.getUrl(UrlMaker.URL_CONTACTS), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("Success")) {
                        Toast.makeText(getActivity(), response.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    ListView listView = getView().findViewById(R.id.listContacts);

                    final List<Map<String,String>> contactData = new ArrayList<>();

                    final JSONArray contacts = response.getJSONArray("Contacts");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject contact = contacts.getJSONObject(i);

                        Map<String,String> contactInfo = new HashMap<>();
                        String contactStr = contact.getString("Contact");
                        String rawContactStr = contactStr;
                        if (isPhone(contactStr)) {
                            contactStr = formatPhone(contactStr);
                        }
                        contactInfo.put("contact", contactStr);
                        contactInfo.put("rawContact", rawContactStr);
                        contactInfo.put("contactId", contact.getString("ContactId"));
                        contactData.add(contactInfo);
                    }

                    Collections.sort(contactData, new contactComp());

                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), contactData,
                            android.R.layout.simple_list_item_1,
                            new String[] {"contact"},
                            new int[] {android.R.id.text1});
                    listView.setAdapter(simpleAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent;

                            String contact = contactData.get(position).get("contact");
                            String rawContact = contactData.get(position).get("rawContact");
                            if (isPhone(rawContact)) {
                                intent = new Intent(view.getContext(), PhoneContactActivity.class);
                            } else {
                                intent = new Intent(view.getContext(), EmailContactActivity.class);
                            }

                            intent.putExtra("IsNew", false);
                            intent.putExtra("Contact", contact);
                            intent.putExtra("ContactId", contactData.get(position).get("contactId"));

                            startActivityForResult(intent, 1);
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

    private void fetchProfile() {
        UrlMaker urlMaker = UrlMaker.getInstance(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, urlMaker.getUrl(UrlMaker.URL_PROFILE), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("Success")) {
                        Toast.makeText(getActivity(), response.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    lastSavedName = response.getString("UsrName");
                    editTextName.setText(lastSavedName);
                    switchSingleMsg.setChecked(response.getBoolean("SingleMsg"));
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

    private void saveProfile() {
        final String nameParam = editTextName.getText().toString().trim();

        UrlMaker urlMaker = UrlMaker.getInstance(getActivity());
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_PROFILE), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(getActivity(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    textViewNameHelp.setText("Name saved");
                    lastSavedName = nameParam;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Unable to save name", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("UsrName", nameParam);
                params.put("SingleMsg", switchSingleMsg.isChecked() ? "true" : "false");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getActivity().getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getActivity().getApplication();
        textViewNameHelp.setText("Saving name");
        app.getRequestQueue().add(stringRequest);
    }

    private String formatPhone(String str) {
        return "(" + str.substring(0, 3) + ") " +
            str.substring(3, 6) + "-" + str.substring(6);
    }

    private boolean isPhone(String str) {
        return str.replaceAll("[^0-9]", "").equals(str);
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
