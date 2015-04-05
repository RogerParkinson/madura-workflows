/*******************************************************************************
 * Copyright (c)2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nz.co.senanque.workflow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * This supports the timestamp format for specific dates
 * 
 * @author Roger Parkinson
 * @version $Revision:$
 */
public class ConvertUtilsDelete
{
    private static final SimpleDateFormat s_dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Date parseDateTime(String d)
    {
        if (d == null)
        {
            return null;
        }
        try {
            return s_dateTimeFormat.parse(d);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static final String printDateTime(Date c)
    {
        if (c == null)
        {
            return null;
        }
        return s_dateTimeFormat.format(c);
    }
}
