/*
 * Copyright (C) 2013 robin.pei(webfanren@gmail.com)
 * 
 * The code is developed under sponsor from Beijing FMSoft Tech. Co. Ltd(http://www.fmsoft.cn)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.espier.voicememos7.ui;

import org.espier.ios7.ui.IPhoneDialogUtil;
import org.espier.voicememos7.R;

import android.app.Activity;
import android.widget.Toast;

public class BaseUi extends Activity {
  public void showMessage(int msg) {
    IPhoneDialogUtil.showTipsDialog(this, R.string.evm7_app_name, msg);
  }

  public void showToast(int msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  }
}
