# for generate 100 different location/blocks

locations = ['Los Angeles', 'San Antonio', 'Houston', 'Chicago', 'Detroit', 'Pittsburgh', 'Atlanta', 'Miami',
             'Jersey City', 'Milwaukee']
blocks = list('ABCDEFGHIJ')
faulty_odds = [.9, 0, .1, .1, .1, .1, .1, .1, .1, .1]

if __name__ == '__main__':
    with open('machine_profiles_medium.csv', 'wt') as f:
        for location in locations:
            for block, pfaulty in zip(blocks, faulty_odds):
                print(f'{location},{block},{pfaulty}', file=f)
