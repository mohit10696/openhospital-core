/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.generaldata.configProvider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.isf.generaldata.GeneralData;
import org.junit.jupiter.api.Test;

public class TestJsonFileConfigProvider {

	@Test
	void testJsonFileConfigProvider() throws Exception {
		GeneralData.initialize();
		JsonFileConfigProvider jsonFileConfigProvider = new JsonFileConfigProvider();

		Map<String, Object> configData = jsonFileConfigProvider.getConfigData();

		assertThat(configData).containsKey("oh_telemetry_url");
		assertThat(configData.get("oh_telemetry_url")).isNotNull();
		assertThat(jsonFileConfigProvider.get("someParam")).isNull();

		// void method
		jsonFileConfigProvider.close();
	}

	@Test
	void testJsonFileConfigProviderBadUrl() throws Exception {
		GeneralData.initialize();
		GeneralData.PARAMSURL = "https://somebadaddress.xxx";

		JsonFileConfigProvider jsonFileConfigProvider = new JsonFileConfigProvider();

		Map<String, Object> configData = jsonFileConfigProvider.getConfigData();

		assertThat(configData).isEmpty();

		assertThat(jsonFileConfigProvider.get("someParam")).isNull();

		// void method
		jsonFileConfigProvider.close();
	}
}
