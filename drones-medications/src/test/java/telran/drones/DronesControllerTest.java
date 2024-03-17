package telran.drones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.spi.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import telran.drones.api.DronesValidationErrorMessages;
import telran.drones.api.ServiceExceptionMessages;
import telran.drones.api.UrlConstants;
import telran.drones.dto.*;
import telran.drones.exceptions.*;
import telran.exceptions.controller.DronesExceptionsController;
import telran.drones.service.DronesService;

record DroneDtoWrongEnum(String number, String modelType) {

}
@AllArgsConstructor
@Getter
class DroneItemsAmountImpl implements DroneItemsAmount {
	String number;
	long amount;
}
@WebMvcTest
class DronesControllerTest {
	@Autowired
	MockMvc mockMvc;
	@MockBean
	DronesService dronesService;
	@Autowired
	ObjectMapper mapper;
	private static final String HOST = "http://localhost:8080/";
	private static final String DRONE_NUMBER_1 = "DRONE-1";
	private static final String MEDICATION_CODE = "MED_1";
	static final String URL_DRONE_REGISTER = HOST + UrlConstants.DRONES;
	private static final String URL_DRONE_LOAD = HOST + UrlConstants.LOAD_DRONE;
	private static final String CONTROLLER_TEST = "Controller:";
	private static final String URL_DRONES_ITEMS = HOST + UrlConstants.DRONE_MEDICATION_ITEMS + DRONE_NUMBER_1;
	private static final String URL_AVAILABLE_DRONES = HOST + UrlConstants.AVAILABLE_DRONES;
	private static final String URL_BATTERY_CAPACITY = HOST + UrlConstants.DRONE_BATTERY_CAPACITY + DRONE_NUMBER_1;
	private static final String URL_ITEMS_AMOUNT = HOST + UrlConstants.DRONES_AMOUNT_ITEMS;
	private static final String URL_HISTORY_LOGS = HOST + UrlConstants.DRONE_HISTORY_LOGS + DRONE_NUMBER_1;
	DroneDto droneDto1 = new DroneDto(DRONE_NUMBER_1, ModelType.Cruiserweight);
	DroneDtoWrongEnum droneDtoWrongFields = new DroneDtoWrongEnum(DRONE_NUMBER_1, "KUKU");
	DroneDto droneDtoMissingFields = new DroneDto(null, null);
	DroneMedication droneMedication = new DroneMedication(DRONE_NUMBER_1, MEDICATION_CODE);
	DroneMedication droneMedicationWrongFields = new DroneMedication(new String(new char[1000]), "mED_1");
	DroneMedication droneMedicationMissingFields = new DroneMedication(null, null);
	String[] errorMessagesDroneWrongFields = {DronesExceptionsController.JSON_TYPE_MISMATCH_MESSAGE};
	String[] errorMessagesDroneMissingFields = { DronesValidationErrorMessages.MISSING_DRONE_NUMBER,
			DronesValidationErrorMessages.MISSING_MODEL};
	String[] errorMessagesDroneMedicationWrongFields = {
			DronesValidationErrorMessages.DRONE_NUMBER_WRONG_LENGTH,
			DronesValidationErrorMessages.WRONG_MEDICATION_CODE
	};
	String[] errorMessagesDroneMedicationMissingFields = {
			DronesValidationErrorMessages.MISSING_DRONE_NUMBER,
			DronesValidationErrorMessages.MISSING_MEDICATION_CODE,
	};
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.REGISTER_DRONE_NORMAL)
		void testDroneRegisterNormal() throws Exception{
			when(dronesService.registerDrone(droneDto1)).thenReturn(droneDto1);
			String droneJSON = mapper.writeValueAsString(droneDto1);
			String response = mockMvc.perform(post(URL_DRONE_REGISTER ).contentType(MediaType.APPLICATION_JSON)
					.content(droneJSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			assertEquals(droneJSON, response);
			
		}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.REGISTER_DRONE_MISSING_FIELDS)
	void testDroneRegisterMissingFields() throws Exception {
		String droneJSON = mapper.writeValueAsString(droneDtoMissingFields);
		String response = mockMvc
				.perform(post(URL_DRONE_REGISTER).contentType(MediaType.APPLICATION_JSON).content(droneJSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertErrorMessages(response, errorMessagesDroneMissingFields);
	}
	private void assertErrorMessages(String response, String[] expectedMessages) {
		String[] actualMessages = response.split(";");
		Arrays.sort(actualMessages);
		Arrays.sort(expectedMessages);
		assertArrayEquals(expectedMessages, actualMessages);
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.REGISTER_DRONE_VALIDATION_VIOLATION)
	void testDronRegisterWrongFields() throws Exception {
		String droneJSON = mapper.writeValueAsString(droneDtoWrongFields);
		String response = mockMvc
				.perform(post(URL_DRONE_REGISTER).contentType(MediaType.APPLICATION_JSON).content(droneJSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertErrorMessages(response, errorMessagesDroneWrongFields);
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.REGISTER_DRONE_ALREADY_EXISTS)
	void testDroneRegisterAlreadyExists() throws Exception{
		when(dronesService.registerDrone(droneDto1)).thenThrow(new DroneAlreadyExistException());
		String droneJSON = mapper.writeValueAsString(droneDto1);
		String response = mockMvc.perform(post(URL_DRONE_REGISTER ).contentType(MediaType.APPLICATION_JSON)
				.content(droneJSON)).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertEquals(ServiceExceptionMessages.DRONE_ALREADY_EXISTS, response);
		
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_NORMAL)
	void loadMedicationNormal() throws Exception{
		when(dronesService.loadDrone(droneMedication)).thenReturn(droneMedication);
		String logDtoJSON = mapper.writeValueAsString(droneMedication);
		String droneMedicationJSON = mapper.writeValueAsString(droneMedication);
		String response = mockMvc.perform(post(URL_DRONE_LOAD).contentType(MediaType.APPLICATION_JSON)
				.content(droneMedicationJSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(logDtoJSON, response);
		
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_MEDICATION_NOT_FOUND)
	void loadMedicationMedicationNotFound() throws Exception {
		serviceExceptionRequest(new MedicationNotFoundException(), 404, ServiceExceptionMessages.MEDICATION_NOT_FOUND);

	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_NOT_FOUND)
	void loadMedicationDroneNotFound() throws Exception {
		serviceExceptionRequest(new DroneNotFoundException(), 404, ServiceExceptionMessages.DRONE_NOT_FOUND);

	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_LOW_BATTERY_CAPCITY)
	void loadMedicationLowBatteryCapacity() throws Exception {
		serviceExceptionRequest(new LowBatteryCapacityException(), 400,
				ServiceExceptionMessages.LOW_BATTERY_CAPACITY);

	}

	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_NOT_MATCHING_STATE)
	void loadMedicationNotMatchingState() throws Exception {
		serviceExceptionRequest(new IllegalDroneStateException(), 400, ServiceExceptionMessages.NOT_IDLE_STATE);

	}

	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_NOT_MATCHING_WEIGHT)
	void loadMedicationNotMatchingWeight() throws Exception {
		serviceExceptionRequest(new IllegalMedicationWeightException(), 400, ServiceExceptionMessages.WEIGHT_LIMIT_VIOLATION);

	}

	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_WRONG_FIELDS)
	void loadMedicationWrongFields() throws Exception {
		validationExceptionRequest(errorMessagesDroneMedicationWrongFields, droneMedicationWrongFields);
	}

	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.LOAD_DRONE_MISSING_FIELDS)
	void loadMedicationMissingFields() throws Exception {
		validationExceptionRequest(errorMessagesDroneMedicationMissingFields, droneMedicationMissingFields);
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_MED_ITEMS_NORMAL)
	void checkMedicationItems()throws Exception {
		String [] itemsExpected = {
				"MED_1", "MED_2"
		};
		when(dronesService.checkMedicationItems(DRONE_NUMBER_1)).thenReturn(List.of(itemsExpected));
		String response = getMethodWithResponse(URL_DRONES_ITEMS);
		assertArrayEquals(itemsExpected, mapper.readValue(response, String[].class));
	}
	private String getMethodWithResponse(String url) throws UnsupportedEncodingException, Exception {
		return mockMvc.perform(get(url)).andExpect(status().isOk())
		.andReturn().getResponse().getContentAsString();
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_MED_ITEMS_DRONE_NOT_FOUND)
	void checkMedicationItemsNotFound()throws Exception {
		
		when(dronesService.checkMedicationItems(DRONE_NUMBER_1)).thenThrow(new DroneNotFoundException());
		mockMvc.perform(get(URL_DRONES_ITEMS)).andExpect(status().isNotFound());
		
		
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.AVAILABLE_DRONES)
	void checkAvailableDrones() throws Exception{
		String[] availableDronesExpected = {
			"DRONE-1", "DRONE-2", "DRONE-10"	
		};
		when(dronesService.checkAvailableDrones()).thenReturn(List.of(availableDronesExpected));
		String response = getMethodWithResponse(URL_AVAILABLE_DRONES);
		assertArrayEquals(availableDronesExpected, mapper.readValue(response, String[].class));
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_BATTERY_LEVEL_NORMAL)
	void checkBatteryCapacityNormal() throws Exception {
		int expected = 50;
		when(dronesService.checkBatteryCapacity(DRONE_NUMBER_1)).thenReturn(expected);
		String response = getMethodWithResponse(URL_BATTERY_CAPACITY);
		assertEquals(Integer.toString(expected), response);
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_BATTERY_LEVEL_DRONE_NOT_FOUND)
	void checkBatteryCapacityNotFound()throws Exception {
		
		when(dronesService.checkBatteryCapacity(DRONE_NUMBER_1)).thenThrow(new DroneNotFoundException());
		mockMvc.perform(get(URL_BATTERY_CAPACITY)).andExpect(status().isNotFound());
		
		
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_LOGS_NORMAL)
	void checkHistoryLogsNormal() throws Exception {
		EventLogDto[] expectedLogs = {
			new EventLogDto(LocalDateTime.now(), DRONE_NUMBER_1, MEDICATION_CODE,
					State.LOADING, 100),
			new EventLogDto(LocalDateTime.now(), DRONE_NUMBER_1, MEDICATION_CODE,
					State.LOADED, 98),
		};
		when(dronesService.checkHistoryLogs(DRONE_NUMBER_1)).thenReturn(List.of(expectedLogs));
		String response = getMethodWithResponse(URL_HISTORY_LOGS);
		assertEquals(mapper.writeValueAsString(expectedLogs), response);
	}
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_LOGS_DRONE_NOT_FOUND)
	void checkHistoryLogsNotFound()throws Exception {
		
		when(dronesService.checkHistoryLogs(DRONE_NUMBER_1)).thenThrow(new DroneNotFoundException());
		mockMvc.perform(get(URL_HISTORY_LOGS)).andExpect(status().isNotFound());
		
		
	}	
	@Test
	@DisplayName(CONTROLLER_TEST + TestDisplayNames.CHECK_DRONES_ITEMS_AMOUNT)
	void checkDronesItemsAmount() throws Exception{
		DroneItemsAmountImpl[] droneItemsExpected = {
			new DroneItemsAmountImpl("DRONE-1", 10)	,
			new DroneItemsAmountImpl("DRONE-2", 9)	,
			new DroneItemsAmountImpl("DRONE-3", 8)	,
			new DroneItemsAmountImpl("DRONe-4", 0)	,
		};
		String expectedJSON = mapper.writeValueAsString(droneItemsExpected);
		when(dronesService.checkDroneLoadedItemAmounts()).thenReturn(List.of(droneItemsExpected));
		String response = getMethodWithResponse(URL_ITEMS_AMOUNT);
		assertEquals(expectedJSON, response);
	}
	

	private void serviceExceptionRequest(RuntimeException serviceException, int statusCode, String errorMessage)
			throws  Exception {
		when(dronesService.loadDrone(droneMedication)).thenThrow(serviceException);
		String droneMedicationJSON = mapper.writeValueAsString(droneMedication);
		String response = mockMvc.perform(post(URL_DRONE_LOAD ).contentType(MediaType.APPLICATION_JSON)
				.content(droneMedicationJSON)).andExpect(status().is(statusCode)).andReturn().getResponse().getContentAsString();
		assertEquals(errorMessage, response);
	}
	private void validationExceptionRequest(String[] errorMessages, DroneMedication droneMedicationDto)
			throws Exception {
		String droneMedicationJSON = mapper.writeValueAsString(droneMedicationDto);
		String response = mockMvc
				.perform(post(URL_DRONE_LOAD).contentType(MediaType.APPLICATION_JSON).content(droneMedicationJSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertErrorMessages(response, errorMessages);
	}
}