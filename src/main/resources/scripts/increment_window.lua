-- Janela fixa do rate limit: incrementa e abre a janela na primeira requisição, atomicamente
-- KEYS[1]: chave do bucket por IP; ARGV[1]: duração da janela em ms
-- Devolve {contagem, ms até a janela fechar}
local n = redis.call('INCR', KEYS[1])
if n == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
return {n, redis.call('PTTL', KEYS[1])}
