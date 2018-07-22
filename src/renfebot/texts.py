"""
Texts to be displayed

"""

import emoji

texts_raw = {

"WELCOME": "Hola, soy tu avisador personal de billetes de Renfe.",
"NOT_AUTH_REPLY": "Hola, {username}, por ahora no estás autorizado a usar este servicio. "
                    "Solicitando acceso a admin. Por favor, intentalo dentro de unos minutos.",
"ADMIN_USER_REQ_ACCESS": "Usuario {username} solicita acceso",
"END_MESSAGE": "Gracias por usar Train Notifier Bot, escribe /start para empezar de nuevo",
"OPTION_SELECTION": "Hola, ¿qué quieres hacer hoy?. Este bot te permite hacer una consulta directa a la web de Renfe o añadir una consulta periódica.",
"MAIN_OP_ADD_QUERY" : "Añadir consulta periódica.",
"MAIN_OP_CHECK_QUERY":"Ver consultas periódicas.",
"MAIN_OP_DEL_QUERY" : "Eliminar consulta periódica.",
"MAIN_OP_DO_QUERY" : "Hacer consulta ahora.",
"MAIN_OP_UNKNOWN": "Lo siento, no te he entendido. ",
"DO_ONETIME_QUERY": "Ok, voy a hacer una consulta puntual.",
"ADD_PERIODIC_QUERY": "Ok, voy a añadir una consulta periódica. Las consultas periódicas se realizarán dos veces al día. Recibirás un mensaje cada vez que se realice la consulta, incluso cuando no haya trenes disponibles.",
"SELECT_TRIP_TO_DETELE": "Por favor, selecciona una consulta para eliminar.",
"SELECT_ORIGIN_STATION": "Introduce estación de origen :station:.",
"SELECT_DESTINATION_STATION": "Introduce estación de destino :station:.",
"SELECT_TRIP_DATE": "Elige ahora la fecha del viaje :date:.",
"SELECTED_TRIP": "Has seleccionado el trayecto {origin} -> {destination}.",
"SELECTED_DATA": "Perfecto, has seleccionado el trayecto {origin} -> {destination}"
                    " para el día {date}.",
"WAIT_FOR_TRAINS": "Espera mientras busco trenes :train:. (Puede tardar un poco...).",
"FOUND_N_TRAINS": "He encontrado {ntrains} trenes :train: con asientos disponibles para"
                    " el trayecto {origin} -> {destination} para el día {date}.",
"NO_TRAINS_FOUND": "No hay trenes para el trayecto {origin} -> {destination} para el día {date}.",
"TRAIN_INFO": "De {t_departure} a {t_arrival}, {cost}€, T: {ticket_type}",
"DB_QUERY_ALREADY": "La consulta periódica ya había sido añadida previamente.",
"DB_QUERY_INSERTED": "La consulta periódica ha sido añadida.",
"DB_QUERY_REMOVED" : "La consulta ha sido eliminada.",
"DB_QUERY_NOT_REMOVED": "Ninguna consulta ha sido eliminada",
"DB_QUERY_NOT_PRESENT": "La consulta periódica no existía.",
"NO_QUERIES_FOR_USERID": "No hay consultas periódicas.",
"QUERIES_FOR_USERID": "Tiene las siguientes consultas periódicas programadas.",
"QUERY_IN_DB": "{date}: {origin} -> {destination}",
"CANCEL" : "Cancelar"
, "ACCESS_GRANTED": "Acceso permitido por admin."
, "ACCESS_NOT_GRANTED": "Acceso no permitido por admin."
}

texts = {}
for t in texts_raw:
    texts[t] = emoji.emojize(texts_raw[t],use_aliases=True)

keyboards = {
"MAIN_OPTIONS": [[texts["MAIN_OP_DO_QUERY"]],
                [texts["MAIN_OP_ADD_QUERY"]],
                [texts["MAIN_OP_DEL_QUERY"]],
                [texts["MAIN_OP_CHECK_QUERY"]]
                ]
,
"STATIONS" : [["MADRID-PUERTA DE ATOCHA",
                "SEVILLA-SANTA JUSTA"],
                ["BARCELONA-SANTS",
                "VALENCIA JOAQUIN SOROLLA"],
                ["ALICANTE/ALACANT",
                "MALAGA MARIA ZAMBRANO"]
            ]
}
