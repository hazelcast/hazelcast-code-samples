def transform_list(input_list):
	# 818604131,818604131,oxygen,unalert,88,95,91,19,215,36.4=2,3,0,0,0,0,0,0=5=Medium
	# "key", "id", "breathing", "consciousness", "heart", "oxygen_saturation_one", "oxygen_saturation_two", "respiratory", "systolic_blood_pressure", "temperature"
	risk_level = []
	for sublist in input_list:
		reading_list = sublist.split(",")
		red_score = False
		breathing_score = int(breathing_score_calc(reading_list[2]))
		consciousness_score = int(consciousness_score_calc(reading_list[3]))
		if consciousness_score == 3:
			red_score = True
		heart_rate_score = int(heart_rate_score_calc(reading_list[4]))
		if heart_rate_score == 3:
			red_score = True
		oxygen_saturation_one_score = int(oxygen_saturation_one_score_calc(reading_list[5]))
		if oxygen_saturation_one_score == 3:
			red_score = True
		oxygen_saturation_two_score = int(oxygen_saturation_two_score_calc(reading_list[6], reading_list[2]))
		if oxygen_saturation_two_score == 3:
			red_score = True
		respiratory_score = int(respiratory_score_calc(reading_list[7]))
		if respiratory_score == 3:
			red_score = True
		systolic_blood_pressure_score = int(systolic_blood_pressure_score_calc(reading_list[8]))
		if systolic_blood_pressure_score == 3:
			red_score = True
		temperature_score = int(temperature_score_calc(reading_list[9]))
		if temperature_score == 3:
			red_score = True

		risk_score = int(breathing_score + consciousness_score + heart_rate_score + oxygen_saturation_one_score + oxygen_saturation_two_score + respiratory_score + systolic_blood_pressure_score + temperature_score)
		risk_scores = [breathing_score, consciousness_score, heart_rate_score, oxygen_saturation_one_score, oxygen_saturation_two_score, respiratory_score, systolic_blood_pressure_score, temperature_score]

		output_string = ""
		output_string += reading_list[0]
		output_string += "="
		output_string += ",".join(map(str, risk_scores))
		output_string += "="

		if 0 <= risk_score <= 4:
			output_string += str(risk_score)
			output_string += "=Low"
		elif (red_score is True) and (risk_score < 5):
			output_string += str(risk_score)
			output_string += "=Low-Medium"
		elif 5 <= risk_score <= 6:
			output_string += str(risk_score)
			output_string += "=Medium"
		elif risk_score >= 7:
			output_string += str(risk_score)
			output_string += "=High"

		risk_level.append(output_string)

	return risk_level


def breathing_score_calc(value):
	result_score = 0
	if value == "air":
		result_score = 0
	elif value == "oxygen":
		result_score = 2
	return result_score


def consciousness_score_calc(value):
	result_score = 0
	if value == "alert":
		result_score = 0
	elif value == "unalert":
		result_score = 3
	return result_score


def heart_rate_score_calc(value):
	result_score = 0
	if int(value) >= 131:
		result_score = 3
	elif 111 <= int(value) <= 130:
		result_score = 2
	elif 91 <= int(value) <= 110:
		result_score = 1
	elif 51 <= int(value) <= 90:
		result_score = 0
	elif 41 <= int(value) <= 50:
		result_score = 1
	elif int(value) <= 40:
		result_score = 3
	return result_score


def oxygen_saturation_one_score_calc(value):
	result_score = 0
	if int(value) >= 96:
		result_score = 0
	elif 94 <= int(value) <= 95:
		result_score = 1
	elif 92 <= int(value) <= 93:
		result_score = 2
	elif int(value) <= 91:
		result_score = 3
	return result_score


def oxygen_saturation_two_score_calc(value1, value2):
	result_score = 0
	if int(value1) >= 97 and value2 == "oxygen":
		result_score = 3
	elif (95 <= int(value1) <= 96) and value2 == "oxygen":
		result_score = 2
	elif (93 <= int(value1) <= 94) and value2 == "oxygen":
		result_score = 1
	elif (88 <= int(value1) <= 92) or (int(value1) >= 93 and value2 == "air"):
		result_score = 0
	elif 86 <= int(value1) <= 87:
		result_score = 1
	elif 84 <= int(value1) <= 85:
		result_score = 2
	elif int(value1) <= 83:
		result_score = 3
	return result_score


def respiratory_score_calc(value):
	result_score = 0
	if int(value) >= 25:
		result_score = 3
	elif 21 <= int(value) <= 24:
		result_score = 2
	elif 12 <= int(value) <= 20:
		result_score = 0
	elif 9 <= int(value) <= 11:
		result_score = 1
	elif int(value) <= 8:
		result_score = 3
	return result_score


def systolic_blood_pressure_score_calc(value):
	result_score = 0
	if int(value) >= 220:
		result_score = 3
	elif 111 <= int(value) <= 219:
		result_score = 0
	elif 101 <= int(value) <= 110:
		result_score = 1
	elif 91 <= int(value) <= 100:
		result_score = 2
	elif int(value) <= 90:
		result_score = 3
	return result_score


def temperature_score_calc(value):
	result_score = 0
	if float(value) >= 39.1:
		result_score = 2
	elif 36.1 <= float(value) <= 38.0:
		result_score = 0
	elif (35.1 <= float(value) <= 36.0) or (38.1 <= float(value) <= 39.0):
		result_score = 1
	elif float(value) <= 35.0:
		result_score = 3
	return result_score
