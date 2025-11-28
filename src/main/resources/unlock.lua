if(redis.call('get',keys[1])==ARGV[1]) then
  return redis.call('del',KEYS[1])
return 0