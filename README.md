# Nutrisafio Mobs
Mod de Fabric 1.21.1 con los mobs nuevos del servidor Nutrisafio.

## Jester
- 20 de vida, velocidad 0.27 (un poco mas rapido que un zombi)
- Explota en 1 segundo (creeper: 1.5 s). Hace dano pero no rompe bloques.
- A los jugadores a menos de 8 bloques les llena la pantalla de confeti por 5 segundos.
- Aparece de noche como un monstruo normal. Suelta polvora y experiencia.
- Invocar: `/summon nutrisafio_mobs:jester`

Se compila solo en GitHub (pestana Actions). El .jar sale en "Artifacts".

## Beast
- 60 de vida (30 corazones), velocidad 0.345 (como un zombi bebe)
- 7 de dano por golpe (como un zombi con espada de piedra)
- Solo aparece durante la Noche de Sangre (`/function noche:activar`): 8% por jugador cada 4 s, maximo 2 cerca.
- Lleva la etiqueta `sangre`, asi desaparece al terminar la noche.
- Suelta 1-3 huesos y 15 de experiencia.
- Invocar: `/summon nutrisafio_mobs:beast`

## Broken Soul
- Nether (todos los biomas), poco comun. No le afecta el fuego.
- Neutral: solo ataca si le pegas. Se calma si mueres, te alejas mucho o pasas a creativo.
- 30 de vida (15 corazones), 8 de dano, velocidad 0.3 (como el Enderman). No se teletransporta.
- Suelta 0-2 Huesos de Wither (`nutrisafio_mobs:wither_bone`) y 10 de experiencia.
- Invocar: `/summon nutrisafio_mobs:broken_soul`

## Quibungo
- De noche en el mundo normal, tan raro como una bruja. Mide unos 3.5 bloques.
- 100 de vida (50 corazones), velocidad 0.28, 8 de dano en golpe normal.
- Te agarra: te sostiene frente a su boca 2 s, te sacude y te muerde 4 veces (6 corazones), luego te lanza. 5 s de espera entre agarres.
- Suelta 1-3 Ojos Malditos (del mod nutrisafio), 1-3 Huesos de Wither y 1 Diente de Quibungo.
- Invocar: `/summon nutrisafio_mobs:quibungo`

## Corazones (comida)
- Corazon: Regeneracion I por 20 s.
- Corazon Superior: Regeneracion II y Resistencia II por 90 s.
- Corazon Dorado: +1 corazon de vida maxima para siempre (maximo 10 extra). Se conserva al morir.
- Los tres se pueden comer aunque estes lleno.

## Gota de Sangre
- La sueltan el Beast y el Quibungo: siempre 1, 35% de una segunda, 15% de una tercera (con Looting sube).

## Recetas
- Corazon: manzana en el centro y 8 Gotas de Sangre alrededor.
- Corazon Superior: Corazon en el centro y 8 lingotes de oro alrededor.
- Corazon Dorado (+1 vida): Corazon al centro, bloques de oro arriba/abajo/izquierda/derecha y Gotas de Sangre en las esquinas.

## Cofre con Candado
- Receta: como un cofre (8 tablas) con un lingote de hierro en el centro.
- Solo quien lo puso puede abrirlo o romperlo (y los operadores en creativo).
- Las tolvas no pueden sacar ni meter cosas. Las explosiones no lo rompen.
- Dos cofres juntos (mismo dueno, mirando al mismo lado) forman un cofre doble de 54 espacios. Agachado se pone sin unir.
- Plantilla de Mejora Maldita: plantilla de mejora de netherita en el centro y 8 Ojos Malditos alrededor.

## Armadura Maldita
- Mesa de herreria: pieza de DIAMANTE + Plantilla de Mejora Maldita + lingote de netherita.
- Igual de fuerte que la netherita (no se quema en lava) y +1 corazon por pieza (4 corazones con el set completo).
- La plantilla se gasta al usarla y NO tiene receta para duplicarla.
- Texturas: iconos de Vicky; la textura puesta (layer_1/2) la dibujo Claude con los mismos colores.
- El Casco Maldito puesto muestra cuernos (CursedHelmetRenderer, Fabric ArmorRenderer).
