package com.app.ekottel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;


import com.app.ekottel.R;
import com.app.ekottel.adapter.CallLogSectionListAdapter;
import com.app.ekottel.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity is used to display Country Name and Country Code.
 *
 * @author Ramesh U
 * @version 2017
 */

public class ZoneSelectActivityTest extends Activity {
    ArrayList<String> zoneList1 = new ArrayList<>();
    ArrayList<String> zoneList2 = new ArrayList<>();
    ArrayList<String> zoneList3 = new ArrayList<>();

    private TextView mMyTextView, mTvCountrySearch, mSearchClearTv;
    ZoneAdapter zoneAdapter;

    ListView listView;
    Typeface textRegular;

    public static Map<String, String> country2Phone = new HashMap<String, String>();

    static {
        country2Phone.put("Abkhazia", "+7840");
        country2Phone.put("Afghanistan", "+93");
        country2Phone.put("Albania", "+355");
        country2Phone.put("Algeria", "+213");
        country2Phone.put("American Samoa", "+1684");
        country2Phone.put("Andorra", "+376");
        country2Phone.put("Angola", "+244");
        country2Phone.put("Anguilla", "+1264");
        country2Phone.put("Antigua and Barbuda", "+1268");
        country2Phone.put("Argentina", "+54");
        country2Phone.put("Armenia", "+374");
        country2Phone.put("Aruba", "+297");
        country2Phone.put("Ascension", "+247");
        country2Phone.put("Australia CASEY", "+6112");
        country2Phone.put("Australia CENTRAL EAST REGION", "+612");
        country2Phone.put("Australia DAVIS", "+6110");
        country2Phone.put("Australia MACQUARIE ISLAND", "+6113");
        country2Phone.put("Australia MAWSON", "+6111");
        country2Phone.put("Australia NORTH EAST REGION", "+617");
        country2Phone.put("Australia SOUTH EAST REGION", "+613");

        country2Phone.put("Australian External Territories", "+672");
        country2Phone.put("Austria", "+43");
        country2Phone.put("Azerbaijan", "+994");
        country2Phone.put("Bahamas", "+1242");
        country2Phone.put("Bahrain", "+973");
        country2Phone.put("Bangladesh", "+880");
        country2Phone.put("Barbados", "+1246");
        country2Phone.put("Barbuda", "+1268");
        country2Phone.put("Belarus", "+375");
        country2Phone.put("Belgium", "+32");
        country2Phone.put("Belize", "+501");
        country2Phone.put("Benin", "+229");
        country2Phone.put("Bermuda", "+1441");
        country2Phone.put("Bhutan", "+975");
        country2Phone.put("Bolivia", "+591");
        country2Phone.put("Bosnia and Herzegovina", "+387");
        country2Phone.put("Botswana", "+267");
        country2Phone.put("Brazil", "+55");
        country2Phone.put("British Indian Ocean Territory", "+246");
        country2Phone.put("British Virgin Islands", "+1284");
        country2Phone.put("Brunei", "+673");
        country2Phone.put("Bulgaria", "+359");
        country2Phone.put("Burkina Faso", "+226");
        country2Phone.put("Burundi", "+257");
        country2Phone.put("Cambodia", "+855");
        country2Phone.put("Cameroon", "+237");
       // country2Phone.put("Canada", "+1");
        country2Phone.put("Canada ALBERTA", "+1587");
        country2Phone.put("Canada CALGARY", "+1403");
        country2Phone.put("Canada EDMONTON", "+1780");
        country2Phone.put("Canada GATINEAU", "+1819");
        country2Phone.put("Canada HALIFAX", "+1902");
        country2Phone.put("Canada LONDON", "+1519");
        country2Phone.put("Canada MISSISSAUGA", "+1905");
        country2Phone.put("Canada MONTREAL", "+1514");
        country2Phone.put("Canada OTTAWA", "+1613");
        country2Phone.put("Canada QUEBEC CITY", "+1581");
        country2Phone.put("Canada SASKATOON", "+1306");
        country2Phone.put("Canada SAULT STE. MARIE", "+1705");
        country2Phone.put("Canada SPECIALIZED TELECOM SERVICES", "+1600");
        country2Phone.put("Canada ST. JOHN", "+1506");
        country2Phone.put("Canada ST. JOHN'S", "+1709");
        country2Phone.put("Canada TERREBONE", "+1450");
        country2Phone.put("Canada THUNBER BAY", "+1807");
        country2Phone.put("Canada TORONTO", "+1647");
        country2Phone.put("Canada VANCOUVER", "+1237");
        country2Phone.put("Canada VICTORIA", "+1250");
        country2Phone.put("Canada WINNIPEG", "+1204");
        country2Phone.put("Canada YELLOWKNIFE", "+1867");


        country2Phone.put("Cape Verde", "+238");
        country2Phone.put("Cayman Islands", "+345");
        country2Phone.put("Central African Republic", "+236");
        country2Phone.put("Chad", "+235");
        country2Phone.put("Chile", "+56");
        country2Phone.put("China", "+86");
        country2Phone.put("Christmas Island", "+61");
        country2Phone.put("Cocos-Keeling Islands", "+61");
        country2Phone.put("Colombia", "+57");
        country2Phone.put("Comoros", "+269");
        country2Phone.put("Congo", "+242");
        country2Phone.put("Congo, Dem. Rep. of (Zaire)", "+243");
        country2Phone.put("Cook Islands", "+682");
        country2Phone.put("Costa Rica", "+506");
        country2Phone.put("Croatia", "+385");
        country2Phone.put("Cuba", "+53");
        country2Phone.put("Curacao", "+599");
        country2Phone.put("Cyprus", "+537");
        country2Phone.put("Czech Republic", "+420");
        country2Phone.put("Denmark", "+45");
        country2Phone.put("Diego Garcia", "+246");
        country2Phone.put("Djibouti", "+253");
        country2Phone.put("Dominica", "+1767");
        country2Phone.put("Dominican Republic", "+1809");
        country2Phone.put("East Timor", "+670");
        country2Phone.put("Easter Island", "+56");
        country2Phone.put("Ecuador", "+593");
        country2Phone.put("Egypt", "+20");
        country2Phone.put("El Salvador", "+503");
        country2Phone.put("Equatorial Guinea", "+240");
        country2Phone.put("Eritrea", "+291");
        country2Phone.put("Estonia", "+372");
        country2Phone.put("Ethiopia", "+251");
        country2Phone.put("Falkland Islands", "+500");
        country2Phone.put("Faroe Islands", "+298");
        country2Phone.put("Fiji", "+679");
        country2Phone.put("Finland", "+358");
        country2Phone.put("France", "+33");
        country2Phone.put("French Antilles", "+596");
        country2Phone.put("French Guiana", "+594");
        country2Phone.put("French Polynesia", "+689");
        country2Phone.put("Gabon", "+241");
        country2Phone.put("Gambia", "+220");
        country2Phone.put("Georgia", "+995");
        country2Phone.put("Germany", "+49");
        country2Phone.put("Ghana", "+233");
        country2Phone.put("Gibraltar", "+350");
        country2Phone.put("Greece", "+30");
        country2Phone.put("Greenland", "+299");
        country2Phone.put("Grenada", "+1473");
        country2Phone.put("Guadeloupe", "+590");
        country2Phone.put("Guam", "+1671");
        country2Phone.put("Guatemala", "+502");
        country2Phone.put("Guinea", "+224");
        country2Phone.put("Guinea-Bissau", "+245");
        country2Phone.put("Guyana", "+595");
        country2Phone.put("Haiti", "+509");
        country2Phone.put("Honduras", "+504");
        country2Phone.put("Hong Kong SAR China", "+852");
        country2Phone.put("Hungary", "+36");
        country2Phone.put("Iceland", "+354");
        country2Phone.put("India", "+91");
        country2Phone.put("Indonesia", "+62");
        country2Phone.put("Iran", "+98");
        country2Phone.put("Iraq", "+964");
        country2Phone.put("Ireland", "+353");
        country2Phone.put("Israel", "+972");
        country2Phone.put("Italy", "+39");
        country2Phone.put("Ivory Coast", "+225");
        country2Phone.put("Jamaica", "+1876");
        country2Phone.put("Japan", "+81");
        country2Phone.put("Jordan", "+962");
        country2Phone.put("Kazakhstan", "+77");
        country2Phone.put("Kenya", "+254");
        country2Phone.put("Kiribati", "+686");
        country2Phone.put("Kuwait", "+965");
        country2Phone.put("Kyrgyzstan", "+996");
        country2Phone.put("Laos", "+856");
        country2Phone.put("Latvia", "+371");
        country2Phone.put("Lebanon", "+961");
        country2Phone.put("Lesotho", "+266");
        country2Phone.put("Liberia", "+231");
        country2Phone.put("Libya", "+218");
        country2Phone.put("Liechtenstein", "+423");
        country2Phone.put("Lithuania", "+370");
        country2Phone.put("Luxembourg", "+352");
        country2Phone.put("Macau SAR China", "+853");
        country2Phone.put("Macedonia", "+389");
        country2Phone.put("Madagascar", "+261");
        country2Phone.put("Malawi", "+265");
        country2Phone.put("Malaysia", "+60");
        country2Phone.put("Maldives", "+960");
        country2Phone.put("Mali", "+223");
        country2Phone.put("Malta", "+356");
        country2Phone.put("Marshall Islands", "+692");
        country2Phone.put("Martinique", "+596");
        country2Phone.put("Mauritania", "+222");
        country2Phone.put("Mauritius", "+230");
        country2Phone.put("Mayotte", "+262");
        country2Phone.put("Mexico", "+52");
        country2Phone.put("Micronesia", "+691");
        country2Phone.put("Midway Island", "+1808");
        country2Phone.put("Moldova", "+373");
        country2Phone.put("Monaco", "+377");
        country2Phone.put("Mongolia", "+976");
        country2Phone.put("Montenegro", "+382");
        country2Phone.put("Montserrat", "+1664");
        country2Phone.put("Morocco", "+212");
        country2Phone.put("Myanmar", "+95");
        country2Phone.put("Namibia", "+264");
        country2Phone.put("Nauru", "+674");
        country2Phone.put("Nepal", "+977");
        country2Phone.put("Netherlands", "+31");
        country2Phone.put("Netherlands Antilles", "+599");
        country2Phone.put("Nevis", "+1869");
        country2Phone.put("New Caledonia", "+687");
        country2Phone.put("New Zealand", "+64");
        country2Phone.put("Nicaragua", "+505");
        country2Phone.put("Niger", "+227");
        country2Phone.put("Nigeria", "+234");
        country2Phone.put("Niue", "+683");
        country2Phone.put("Norfolk Island", "+672");
        country2Phone.put("North Korea", "+850");
        country2Phone.put("Northern Mariana Islands", "+1670");
        country2Phone.put("Norway", "+47");
        country2Phone.put("Oman", "+968");
        country2Phone.put("Pakistan", "+92");
        country2Phone.put("Palau", "+680");
        country2Phone.put("Palestinian Territory", "+970");
        country2Phone.put("Panama", "+507");
        country2Phone.put("Papua New Guinea", "+675");
        country2Phone.put("Paraguay", "+595");
        country2Phone.put("Peru", "+51");
        country2Phone.put("Philippines", "+63");
        country2Phone.put("Poland", "+48");
        country2Phone.put("Portugal", "+351");
        country2Phone.put("Puerto Rico", "+1787");
        country2Phone.put("Qatar", "+974");
        country2Phone.put("Reunion", "+262");
        country2Phone.put("Romania", "+40");
        country2Phone.put("Russia", "+7");
        country2Phone.put("Rwanda", "+250");
        country2Phone.put("Samoa", "+685");
        country2Phone.put("San Marino", "+378");
        country2Phone.put("Saudi Arabia", "+966");
        country2Phone.put("Senegal", "+221");
        country2Phone.put("Serbia", "+381");
        country2Phone.put("Seychelles", "+248");
        country2Phone.put("Sierra Leone", "+232");
        country2Phone.put("Singapore", "+65");
        country2Phone.put("Slovakia", "+421");
        country2Phone.put("Slovenia", "+386");
        country2Phone.put("Solomon Islands", "+677");
        country2Phone.put("South Africa", "+27");
        country2Phone.put("South Georgia and the South Sandwich Islands", "+500");
        country2Phone.put("South Korea", "+82");
        country2Phone.put("Spain", "+34");
        country2Phone.put("Sri Lanka", "+94");
        country2Phone.put("Sudan", "+249");
        country2Phone.put("Suriname", "+597");
        country2Phone.put("Swaziland", "+268");
        country2Phone.put("Sweden", "+46");
        country2Phone.put("Switzerland", "+41");
        country2Phone.put("Syria", "+963");
        country2Phone.put("Taiwan", "+886");
        country2Phone.put("Tajikistan", "+992");
        country2Phone.put("Tanzania", "+255");
        country2Phone.put("Thailand", "+66");
        country2Phone.put("Timor Leste", "+670");
        country2Phone.put("Togo", "+228");
        country2Phone.put("Tokelau", "+690");
        country2Phone.put("Tonga", "+676");
        country2Phone.put("Trinidad and Tobago", "+1868");
        country2Phone.put("Tunisia", "+216");
        country2Phone.put("Turkey", "+90");
        country2Phone.put("Turkmenistan", "+993");
        country2Phone.put("Turks and Caicos Islands", "+1649");
        country2Phone.put("Tuvalu", "+688");
        country2Phone.put("U.S. Virgin Islands", "+1340");
        country2Phone.put("Uganda", "+256");
        country2Phone.put("Ukraine", "+380");
        country2Phone.put("United Arab Emirates", "+971");
        country2Phone.put("United Kingdom", "+44");
        country2Phone.put("United States", "+1");
        country2Phone.put("Uruguay", "+598");
        country2Phone.put("Uzbekistan", "+998");
        country2Phone.put("Vanuatu", "+678");
        country2Phone.put("Venezuela", "+58");
        country2Phone.put("Vietnam", "+84");
        country2Phone.put("Wake Island", "+1808");
        country2Phone.put("Wallis and Futuna", "+681");
        country2Phone.put("Yemen", "+967");
        country2Phone.put("Zambia", "+260");
        country2Phone.put("Zanzibar", "+255");
        country2Phone.put("Zimbabwe", "+263");
    }

    private String TAG1;

    private TextView mBackButton, mTittleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zone_select);
        TAG1 = getString(R.string.zone_no_matches_found);

        listView = (ListView) findViewById(R.id.listViewZone);
        final EditText slectcountry = (EditText) findViewById(R.id.search_country);
        mTvCountrySearch = (TextView) findViewById(R.id.tv_country_search);
        mMyTextView = (TextView) findViewById(R.id.tv_no_country);
        mBackButton = findViewById(R.id.tv_zone_select_back_arrow);
        mTittleTextView = findViewById(R.id.tv_header_zone_select);

        mMyTextView.setVisibility(View.INVISIBLE);


        textRegular = Utils.getTypefaceRegular(getApplicationContext());
        slectcountry.setTypeface(textRegular);
        mMyTextView.setTypeface(textRegular);
        mTittleTextView.setTypeface(Utils.getTypefaceMedium(getApplicationContext()));
      EditText  mSearchClearTv = findViewById(R.id.search_country);

        Typeface text_font = Utils.getTypeface(getApplicationContext());
        mTvCountrySearch.setTypeface(text_font);
        mBackButton.setTypeface(text_font);
        mSearchClearTv.setTypeface(text_font);

        mTvCountrySearch.setText(getResources().getString(R.string.country_search));

        final ArrayList<String> zoneList = getCountries();
        zoneList1 = getCountries();

        for (String str : zoneList) {
            zoneList2.add(str);
            zoneList3.add(str);
        }

      //  Log.i(TAG1, "Zone List" + zoneList2);
        //Log.i(TAG1, "Zone List" + zoneList1);

        zoneAdapter = new ZoneAdapter(this, zoneList2);

        CallLogSectionListAdapter sectionAdapter = new CallLogSectionListAdapter(
                getApplicationContext(), this.getLayoutInflater(), zoneAdapter, zoneList2);

        listView.setAdapter(sectionAdapter);

        if (listView.getCount() <= 0) {
            mMyTextView.setVisibility(View.VISIBLE);
        } else {
            mMyTextView.setVisibility(View.INVISIBLE);
        }
        mSearchClearTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                slectcountry.setText("");
            }
        });
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                TextView zoneTextView = ((TextView) v
                        .findViewById(R.id.tv_zone_adapter));
                if (zoneTextView != null && zoneTextView.getText() != null) {
                    String zoneName = zoneTextView.getText().toString().trim();
                    Intent intent = new Intent();

                    intent.putExtra(getString(R.string.zone_intent_zone), zoneName);
                    setResult(999, intent);
                    finish();
                }
            }
        });


        slectcountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                if(charSequence.toString().length()>0){

                    mSearchClearTv.setVisibility(View.VISIBLE);
                }else {
                    mSearchClearTv.setVisibility(View.GONE);
                }

                //Log.i(TAG1, "Yes on touch up:" + slectcountry.getText().toString());
                if (slectcountry.getText().toString().equals("")) {
                    zoneList2.clear();
                    zoneList3.clear();
                    zoneList1.clear();
                    zoneList1 = getCountries();
                    for (String str : zoneList) {
                        zoneList2.add(str);
                        zoneList3.add(str);
                    }

                } else {
                    zoneList2.clear();
                    zoneList1.clear();

                    for (int ii = 0; ii < zoneList3.size(); ii++) {
                        String str = zoneList3.get(ii);

                        String str1 = str.toLowerCase();
                        String str2 = slectcountry.getText().toString().toLowerCase();
                        if (str1.contains(str2)) {

                            zoneList2.add(str);
                            zoneList1.add(zoneList.get(ii));

                        }
                    }
                }

                zoneAdapter = new ZoneAdapter(ZoneSelectActivityTest.this, zoneList2);
                CallLogSectionListAdapter sectionAdapter = new CallLogSectionListAdapter(
                        getApplicationContext(), ZoneSelectActivityTest.this.getLayoutInflater(), zoneAdapter, zoneList2);

                listView.setAdapter(sectionAdapter);
                if (listView.getCount() <= 0) {
                    mMyTextView.setVisibility(View.VISIBLE);
                } else {
                    mMyTextView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }


    /**
     * Method that handle country name and country code
     *
     * @return list of country name and country code
     */
    public ArrayList<String> getCountries() {
        ArrayList<String> countries = new ArrayList<String>();

        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");

            countries.add(g[2] + "(" + g[1] + ")");

        }
        return countries;
    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            //Utils.handleonresume();
            mMyTextView.setVisibility(View.INVISIBLE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

      /*  try {
            Utils.handleonpause();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    /*
    This is used to display country names and country codes
     */
    public class ZoneAdapter extends BaseAdapter implements Filterable {

        ArrayList<String> zonearray;


        LayoutInflater inflater = null;
        Context mcontext;
        ZonesFilter zonefilter = new ZonesFilter();

        public ZoneAdapter(Context mcontext, ArrayList<String> zonearray) {

            this.zonearray = zonearray;
            this.mcontext = mcontext;
            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        }

        @Override
        public int getCount() {
            if (zonearray != null && zonearray.size() > 0) {
                return zonearray.size();
            } else {
                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder {
            TextView name_tv;

        }

        @Override
        public View getView(final int position, View rowView, ViewGroup parent) {
            Holder holder = new Holder();
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.zone_adapter, null);
                holder.name_tv = (TextView) rowView
                        .findViewById(R.id.tv_zone_adapter);

                holder.name_tv.setTypeface(textRegular);
                rowView.setTag(holder);
            } else {
                holder = (Holder) rowView.getTag();
            }

            holder.name_tv.setText(zonearray.get(position));


            return rowView;
        }


        @Override
        public Filter getFilter() {
            return zonefilter;
        }

        /*
        This is used to filter given string and display filtered list
         */
        public class ZonesFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // NOTE: this function is *always* called from a background
                // thread,

                //Log.i(TAG1, "Search entered text: " + constraint);

                FilterResults result = new FilterResults();

                if (constraint != null && constraint.toString().length() > 0) {

                    String filterString = constraint.toString().toLowerCase();

                    ArrayList<String> Items = new ArrayList<String>();
                    ArrayList<String> filterList = new ArrayList<String>();

                    synchronized (this) {

                        Items = zonearray;
                    }
                    for (int i = 0; i < Items.size(); i++) {


                        if (Items.get(i).toString().toLowerCase()
                                .contains(filterString.toLowerCase())) {

                            filterList.add(Items.get(i));

                        }

                    }

                    result.count = filterList.size();
                    result.values = filterList;

                } else {

                    synchronized (this) {

                        result.count = zonearray.size();
                        result.values = zonearray;

                    }

                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                @SuppressWarnings("unchecked")
                ArrayList<String> filtered = (ArrayList<String>) results.values;

                if (filtered != null) {

                    if (filtered.size() > 0) {

                        zoneAdapter = new ZoneAdapter(ZoneSelectActivityTest.this, filtered);
                        CallLogSectionListAdapter sectionAdapter = new CallLogSectionListAdapter(
                                getApplicationContext(), ZoneSelectActivityTest.this.getLayoutInflater(), zoneAdapter, filtered);

                        listView.setAdapter(sectionAdapter);
                        listView.setEmptyView(mMyTextView);
                        mMyTextView.setVisibility(View.GONE);

                    } else {
                        zoneAdapter = new ZoneAdapter(ZoneSelectActivityTest.this, filtered);
                        CallLogSectionListAdapter sectionAdapter = new CallLogSectionListAdapter(
                                getApplicationContext(), ZoneSelectActivityTest.this.getLayoutInflater(), zoneAdapter, filtered);

                        listView.setAdapter(sectionAdapter);

                        listView.setEmptyView(mMyTextView);
                        mMyTextView.setVisibility(View.VISIBLE);

                    }
                }

            }

        }


    }

}
