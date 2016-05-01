dict = {}

lines = [line.rstrip('\n') for line in open('edges.txt')]
for line in lines:
    node1 = line.split()[0]
    node2 = line.split()[1]
    dict[node1] = 1
    dict[node2] = 1

print 'total nodes number:' + str(len(dict))
